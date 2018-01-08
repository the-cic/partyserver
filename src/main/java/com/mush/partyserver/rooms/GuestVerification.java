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

    public void verifyToken(String token) throws RoomsException {
        HashMap<String, String> tokenUsers = config.getTokenUsers();
        if (tokenUsers.containsKey(token)) {
            logger.info("Token is valid: {}, belongs to {}", token, tokenUsers.get(token));
        } else {
            logger.info("Invalid token: {}", token);
            throw new RoomsException("Invalid token");
        }
    }

}
