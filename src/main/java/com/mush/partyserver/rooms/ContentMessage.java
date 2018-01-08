/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * 
 * @author cic
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class ContentMessage {
    
    public String subject;
    
    public Map<String, Object> body;

}
