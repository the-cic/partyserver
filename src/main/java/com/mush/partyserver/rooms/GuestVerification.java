/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms;

import com.mush.partyserver.Config;
import com.mush.partyserver.rooms.exceptions.RoomsException;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author cic
 */
public class GuestVerification {

    private final Logger logger;
    private final Config config;

    public GuestVerification(Config config0) {
        logger = LogManager.getLogger(this.getClass());
        this.config = config0;
    }

    public void verifyToken(String token, String name) throws RoomsException {
        HashMap<String, String> tokenUsers = config.getTokenUsers();
        if (tokenUsers.containsKey(token)) {
            String user = tokenUsers.get(token);
            if (!user.equals(name)) {
                logger.info("Invalid user {} for token: {}", user, token);
                throw new RoomsException("Invalid user for token");
            }
            logger.info("Token is valid: {}, belongs to {}", token, user);
        } else {
            logger.info("Invalid token: {}", token);
            throw new RoomsException("Invalid token");
        }
    }

    public void verifyPreferredRoomName(String room, String name) throws RoomsException {
        //[rooms]
        //TEST = test
        HashMap<String, String> userRooms = config.getUserRooms();
        if (userRooms.containsKey(room) && userRooms.get(room).equals(name)) {
            logger.info("Preferred room {} for user {} ok", room, name);
        } else {
            logger.info("Invalid preferred room {} for user {}", room, name);
            throw new RoomsException("Invalid preferred room for user");
        }
    }

}
