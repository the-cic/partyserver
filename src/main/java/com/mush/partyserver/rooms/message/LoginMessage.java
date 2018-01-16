/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms.message;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author cic
 */
public class LoginMessage {
    public String login;
    
    @JsonProperty(required = false)
    public String room;
    
    @JsonProperty(required = false)
    public String token;
}
