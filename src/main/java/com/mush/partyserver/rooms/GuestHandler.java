/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms;

import com.mush.partyserver.rooms.exceptions.RoomsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public GuestHandler() {
        logger = LogManager.getLogger(this.getClass());
        rooms = new Rooms();

//        rooms.createNewRoom();
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

    private void login(String message, Guest guest) throws IOException, RoomsException {
        ObjectMapper mapper = new ObjectMapper();

        LoginMessage loginMessage = mapper.readValue(message, LoginMessage.class);

        if (loginMessage.token != null) {
            verifyToken(loginMessage.token);

            String roomName = rooms.createNewRoom();

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
        content.put("room", owner.getRoom());
        sendMessageToOwner(owner, content);
    }

    private void sendGuestConnected(Guest owner, Guest guest) {
        HashMap<String, Object> content = new HashMap<>();
        content.put("connected", guest.getLoginName());
        sendMessageToOwner(owner, content);
    }

    private void sendGuestDisonnected(Guest owner, Guest guest) {
        HashMap<String, Object> content = new HashMap<>();
        content.put("disconnected", guest.getLoginName());
        sendMessageToOwner(owner, content);
    }

    private void sendMessageToOwner(Guest owner, HashMap<String, Object> content) {
        Message message = new Message();
        message.target = "";
        message.content = content;
        owner.send(jsonForObject(message));
    }

    private void verifyToken(String token) {
        // TODO
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

    private void processMessage(String messageString, Guest guest) throws IOException, RoomsException {
        ObjectMapper mapper = new ObjectMapper();

        Message message = mapper.readValue(messageString, Message.class);

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
