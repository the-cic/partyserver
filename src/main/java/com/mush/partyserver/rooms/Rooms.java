/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms;

import com.mush.partyserver.rooms.exceptions.NameAlreadyInRoomException;
import com.mush.partyserver.rooms.exceptions.RoomDoesNotExistException;
import com.mush.partyserver.rooms.exceptions.RoomsException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author cic
 */
public class Rooms {

    /**
     * Map by room name, of client name -> client info
     */
    private HashMap<String, HashMap<String, Guest>> rooms;
    private HashMap<String, Guest> roomOwners;
    private HashMap<String, String> roomTokens;
    private Logger logger;

    public Rooms() {
        logger = LogManager.getLogger(this.getClass());
        rooms = new HashMap<>();
        roomOwners = new HashMap<>();
        roomTokens = new HashMap<>();
    }

    /**
     * Create new room and return its name
     *
     * @param token
     * @return
     * @throws com.mush.partyserver.rooms.exceptions.RoomsException
     */
    public String createNewRoom(String token) throws RoomsException {
        if (roomTokens.containsValue(token)) {
            throw new RoomsException("Room for this token is in use");
        }
        String roomName = newRoomName();
        rooms.put(roomName, new HashMap<>());
        roomTokens.put(roomName, token);
        logger.info("New room created: {}", roomName);
        return roomName;
    }

    public boolean roomExists(String roomName) {
        return rooms.containsKey(roomName);
    }

    public boolean roomHasGuest(String roomName, String guestName) {
        return roomExists(roomName) && rooms.get(roomName).containsKey(guestName);
    }

    public boolean roomHasOwner(String roomName) {
        return roomOwners.containsKey(roomName);
    }

    public Guest getRoomOwner(String roomName) throws RoomDoesNotExistException {
        if (!roomExists(roomName)) {
            throw new RoomDoesNotExistException(roomName);
        }
        return roomOwners.get(roomName);
    }

    public void setRoomOwner(String roomName, Guest owner) throws RoomDoesNotExistException {
        if (!roomExists(roomName)) {
            throw new RoomDoesNotExistException(roomName);
        }
        roomOwners.put(roomName, owner);
    }

    public void addGuestToRoom(String roomName, Guest guest) throws RoomsException {
        if (!roomExists(roomName)) {
            guest.clearRoom();
            throw new RoomDoesNotExistException(roomName);
        }
        if (roomHasGuest(roomName, guest.getLoginName())) {
            guest.clearRoom();
            throw new NameAlreadyInRoomException(guest.getLoginName());
        }
        HashMap<String, Guest> room = rooms.get(roomName);
        room.put(guest.getLoginName(), guest);
        logger.info("Guest {} added to room {}", guest, roomName);
    }

    public void removeGuest(Guest guest) throws RoomsException {
        if (!roomExists(guest.getRoom())) {
            guest.clearRoom();
            throw new RoomDoesNotExistException(guest.getRoom());
        }
        HashMap<String, Guest> room = rooms.get(guest.getRoom());
        Guest guestInRoom = room.get(guest.getLoginName());
        if (guest.equals(guestInRoom)) {
            room.remove(guest.getLoginName());
            logger.info("Guest {} removed from room {}", guest, guest.getRoom());
            guest.clearRoom();
        }
    }

    public Guest getRoomGuest(String roomName, String guestName) throws RoomsException {
        if (!roomExists(roomName)) {
            throw new RoomDoesNotExistException(roomName);
        }
        HashMap<String, Guest> room = rooms.get(roomName);
        return room.get(guestName);
    }

    public Collection<Guest> getRoomGuests(String roomName) throws RoomsException {
        if (!roomExists(roomName)) {
            throw new RoomDoesNotExistException(roomName);
        }
        HashMap<String, Guest> room = rooms.get(roomName);
        return room.values();
    }

    public void closeRoom(String roomName) throws RoomsException {
        if (!roomExists(roomName)) {
            throw new RoomDoesNotExistException(roomName);
        }
        HashMap<String, Guest> room = rooms.get(roomName);
        for (Map.Entry<String, Guest> e : room.entrySet()) {
            Guest guest = e.getValue();
            logger.info("Kicking guest: {}", guest);
            guest.kick();
        }
        room.clear();
        rooms.remove(roomName);
        roomTokens.remove(roomName);
        roomOwners.remove(roomName);
        logger.info("Room closed: {}", roomName);
    }

    private String newRoomName() throws RoomsException {
        int tries = 3;
        String name = getRandomName();
        while (tries > 0 && roomExists(name)) {
            name = getRandomName();
            tries--;
        }
        if (roomExists(name)) {
            throw new RoomsException("Could not create unique room name");
        }
        return name;
    }

    private String getRandomName() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            int c = 'A' + (char) (Math.random() * ('Z' - 'A'));
            sb.append((char) c);
        }

        return sb.toString();
    }
}
