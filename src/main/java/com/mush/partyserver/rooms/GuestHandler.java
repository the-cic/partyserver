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
import java.util.Collection;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author cic
 */
public class GuestHandler {

    private Logger logger;
    private Rooms rooms;
    private Config config;

    public GuestHandler(Config config0) {
        logger = LogManager.getLogger(this.getClass());
        rooms = new Rooms();
        config = config0;
    }

    public void onNewGuest(Guest guest) {
        // guest is not yet logged in
    }

    public void onGuestLeft(Guest guest) {
        logger.info("Guest left: {}", guest);
        if (guest.isLoggedIn()) {
            try {
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
            try {
                login(message, guest);

            } catch (IOException | RoomsException ex) {
                logger.info("Guest {} failed to login: {}", guest, ex.getMessage());
                guest.send(jsonForError(ex.getMessage()));

                logger.info("Kicking guest: " + guest);
                guest.kick();
            }
        } else {
            try {
                processMessage(message, guest);

            } catch (IOException | RoomsException ex) {
                logger.info("Message from {} failed : {}", guest, ex.getMessage());
                guest.send(jsonForError(ex.getMessage()));
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
    private void login(String message, Guest guest) throws IOException, RoomsException {
        ObjectMapper mapper = new ObjectMapper();

        LoginMessage loginMessage = mapper.readValue(message, LoginMessage.class);

        if (loginMessage.token != null) {
            verifyToken(loginMessage.token);

            String roomName = rooms.createNewRoom(loginMessage.token);

            guest.login(loginMessage.login, roomName, true);

            rooms.setRoomOwner(roomName, guest);

        } else {
            guest.login(loginMessage.login, loginMessage.room, false);
        }

        rooms.addGuestToRoom(guest.getRoom(), guest);

        if (guest.getIsRoomOwner()) {
            sendNewRoom(guest);
        } else if (rooms.roomHasOwner(guest.getRoom())) {
            Guest owner = rooms.getRoomOwner(guest.getRoom());
            sendGuestConnected(owner, guest);
        }
    }

    private void sendNewRoom(Guest owner) {
        HashMap<String, Object> content = new HashMap<>();
        content.put("name", owner.getRoom());
        sendMessageToOwner(owner, "roomCreated", content);
    }

    private void sendGuestConnected(Guest owner, Guest guest) {
        HashMap<String, Object> content = new HashMap<>();
        content.put("name", guest.getLoginName());
        sendMessageToOwner(owner, "userConnected", content);
    }

    private void sendGuestDisonnected(Guest owner, Guest guest) {
        HashMap<String, Object> content = new HashMap<>();
        content.put("name", guest.getLoginName());
        sendMessageToOwner(owner, "userDisconnected", content);
    }

    private void sendMessageToOwner(Guest owner, String subject, HashMap<String, Object> body) {
        ContentMessage message = new ContentMessage();
        message.target = "";
        message.subject = subject;
        message.body = body;
        owner.send(jsonForObject(message));
    }

    private void verifyToken(String token) throws RoomsException {
        HashMap<String, String> tokenUsers = config.getTokenUsers();
        if (tokenUsers.containsKey(token)) {
            logger.info("Token is valid: {}, belongs to {}", token, tokenUsers.get(token));
        } else {
            logger.info("Invalid token: {}", token);
            throw new RoomsException("Invalid token");
        }
    }

    private void checkGuestIsOwner(Guest guest) throws RoomsException {
        if (rooms.roomHasOwner(guest.getRoom()) && guest.getIsRoomOwner()) {
            Guest owner = rooms.getRoomOwner(guest.getRoom());
            if (owner.equals(guest)) {
                return;
            }
        }
        throw new RoomsException("Guest is not room owner");
    }

    private void checkGuestIsNotOwner(Guest guest) throws RoomsException {
        if (guest.getIsRoomOwner()) {
            throw new RoomsException("Guest is room owner");
        }
    }

    /**
     * Route the message to the proper recipient(s)
     * @param messageString
     * @param guest
     * @throws IOException
     * @throws RoomsException 
     */
    private void processMessage(String messageString, Guest guest) throws IOException, RoomsException {
        ObjectMapper mapper = new ObjectMapper();

        ContentMessage message = mapper.readValue(messageString, ContentMessage.class);

        switch (message.target) {
            case "":
                checkGuestIsNotOwner(guest);
                Guest owner = rooms.getRoomOwner(guest.getRoom());
                owner.send(messageString);
                break;
            case "*":
                checkGuestIsOwner(guest);
                Collection<Guest> guests = rooms.getRoomGuests(guest.getRoom());
                for (Guest targetGuest : guests) {
                    if (!guest.equals(targetGuest)) {
                        targetGuest.send(messageString);
                    }
                }
                break;
            default:
                checkGuestIsOwner(guest);
                Guest targetGuest = rooms.getRoomGuest(guest.getRoom(), message.target);
                targetGuest.send(messageString);
        }
    }

    private String jsonForError(String error) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("error", error);
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

}
