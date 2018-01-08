/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms;

import com.mush.partyserver.rooms.exceptions.RoomsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mush.partyserver.Config;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author cic
 */
public class GuestHandler {

    private static final String SUBJECT_LOGIN_ACCEPTED = "loginAccepted";
    private static final String SUBJECT_ROOM_CREATED = "roomCreated";
    private static final String SUBJECT_USER_CONNECTED = "userConnected";
    private static final String SUBJECT_USER_DISCONNECTED = "userDisconnected";
    private static final String SUBJECT_COMMAND = "command";
    private static final String SUBJECT_USER_RESPONSE = "userResponse";

    private static final String BODY_ROOM_NAME = "name";
    private static final String BODY_GUEST_NAME = "name";

    private final Logger logger;
    private final Rooms rooms;
    private final GuestVerification verification;

    public GuestHandler(Config config0) {
        logger = LogManager.getLogger(this.getClass());
        rooms = new Rooms();
        verification = new GuestVerification(config0);
    }

    public void onNewGuest(Guest guest) {
        logger.info("New guest connected: {}", guest);
    }

    public void onGuestLeft(Guest guest) {
        logger.info("Guest left: {}", guest);
        if (guest.isLoggedIn()) {
            try {
                // to rooms
                if (guest.getIsRoomOwner()) {
                    logger.info("Leaving guest is room {} owner: {}", guest.getRoom(), guest);
                    rooms.closeRoom(guest.getRoom());
                } else if (rooms.roomHasOwner(guest.getRoom())) {
                    Guest owner = rooms.getRoomOwner(guest.getRoom());
                    sendGuestDisonnected(owner, guest);
                }
                rooms.removeGuest(guest);

            } catch (RoomsException ex) {
                logger.info(ex.getMessage());
            }
        }
    }

    public void onMessage(String message, Guest guest) {
        if (!guest.isLoggedIn()) {
            // First message from new client must be login
            login(message, guest);
        } else {
            try {
                if (rooms.isGuestARoomOwner(guest)) {
                    processOwnerMessage(message, guest);
                } else {
                    processGuestMessage(message, guest);
                }

            } catch (IOException | RoomsException ex) {
                logger.info("Message from {} failed : {}", guest, ex.getMessage());
                guest.send(jsonForException(ex));
            }
        }
    }

    /**
     * Login a guest to an existing room, or create a new room for a room owner
     *
     * @param message
     * @param guest
     * @throws IOException
     * @throws RoomsException
     */
    private void login(String message, Guest guest) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            LoginMessage loginMessage = mapper.readValue(message, LoginMessage.class);

            if (loginMessage.token != null) {
                verification.verifyToken(loginMessage.token);

                String roomName = rooms.createNewRoom(loginMessage.token);

                guest.login(loginMessage.login, roomName, true);

                rooms.setRoomOwner(roomName, guest);

            } else {
                guest.login(loginMessage.login, loginMessage.room, false);
            }

            rooms.addGuestToRoom(guest.getRoom(), guest);

            sendGuestLoginAccepted(guest);

            if (guest.getIsRoomOwner()) {
                sendNewRoom(guest);
            } else if (rooms.roomHasOwner(guest.getRoom())) {
                Guest owner = rooms.getRoomOwner(guest.getRoom());
                sendGuestConnected(owner, guest);
            }

        } catch (IOException | RoomsException ex) {
            logger.info("Guest {} failed to login: {}", guest, ex.getMessage());
            guest.send(jsonForException(ex));

            logger.info("Kicking guest: " + guest);
            guest.kick();
        }
    }

    /**
     * Route the message to the proper recipient(s)
     *
     * @param messageString
     * @param guest
     * @throws IOException
     * @throws RoomsException
     */
    private void processOwnerMessage(String messageString, Guest owner) throws IOException, RoomsException {
        ObjectMapper mapper = new ObjectMapper();

        OwnerToGuestsContentMessage inputMessage = mapper.readValue(messageString, OwnerToGuestsContentMessage.class);
        ContentMessage outputMessage = new ContentMessage();

        outputMessage.subject = SUBJECT_COMMAND;
        outputMessage.body = inputMessage.body;

        String outputString = jsonForObject(outputMessage);

        for (String targetName : inputMessage.recipients) {
            Guest targetGuest = rooms.getRoomGuest(owner.getRoom(), targetName);
            targetGuest.send(outputString);
        }
    }

    /**
     * Route the message to the owner
     *
     * @param messageString
     * @param guest
     * @throws IOException
     * @throws RoomsException
     */
    private void processGuestMessage(String messageString, Guest guest) throws IOException, RoomsException {
        ObjectMapper mapper = new ObjectMapper();

        GuestToOwnerContentMessage message = new GuestToOwnerContentMessage();

        message.from = guest.getLoginName();
        message.subject = SUBJECT_USER_RESPONSE;
        message.body = mapper.readValue(messageString, Map.class);

        Guest owner = rooms.getRoomOwner(guest.getRoom());
        owner.send(jsonForObject(message));
    }

    private String jsonForException(Exception ex) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("error", ex.getClass().getSimpleName());
        map.put("errorDescription", ex.getMessage());
        return jsonForObject(map);
    }

    private String jsonForObject(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            return null;
        }
        return json;
    }

    private void sendNewRoom(Guest owner) {
        HashMap<String, Object> body = new HashMap<>();
        body.put(BODY_ROOM_NAME, owner.getRoom());
        sendMessageToGuest(owner, SUBJECT_ROOM_CREATED, body);
    }

    private void sendGuestConnected(Guest owner, Guest guest) {
        HashMap<String, Object> body = new HashMap<>();
        body.put(BODY_GUEST_NAME, guest.getLoginName());
        sendMessageToGuest(owner, SUBJECT_USER_CONNECTED, body);
    }

    private void sendGuestLoginAccepted(Guest guest) {
        sendMessageToGuest(guest, SUBJECT_LOGIN_ACCEPTED, null);
    }

    private void sendGuestDisonnected(Guest owner, Guest guest) {
        HashMap<String, Object> body = new HashMap<>();
        body.put(BODY_GUEST_NAME, guest.getLoginName());
        sendMessageToGuest(owner, SUBJECT_USER_DISCONNECTED, body);
    }

    private void sendMessageToGuest(Guest guest, String subject, HashMap<String, Object> body) {
        ContentMessage message = new ContentMessage();
        message.subject = subject;
        message.body = body;
        guest.send(jsonForObject(message));
    }

}
