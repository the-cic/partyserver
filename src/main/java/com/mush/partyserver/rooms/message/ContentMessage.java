/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * Base message with subject and content
 * 
 * @author cic
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class ContentMessage {
    
    public static final String SUBJECT_LOGIN_ACCEPTED = "loginAccepted";
    public static final String SUBJECT_ROOM_CREATED = "roomCreated";
    public static final String SUBJECT_USER_CONNECTED = "userConnected";
    public static final String SUBJECT_USER_DISCONNECTED = "userDisconnected";
    public static final String SUBJECT_ERROR = "error";
    public static final String SUBJECT_COMMAND = "command";
    public static final String SUBJECT_USER_RESPONSE = "userResponse";

    public static final String BODY_ROOM_NAME = "name";
    public static final String BODY_GUEST_NAME = "name";
    public static final String BODY_ERROR = "error";
    public static final String BODY_ERROR_DESCRIPTION = "errorDescription";
    
    public String subject;
    
    public Map<String, Object> body;

}
