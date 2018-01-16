/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Message from the owner to the server for delivery to guests.
 *
 * @author cic
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class OwnerMessage extends ContentMessage {

    @JsonProperty(value = "to")
    public List<String> recipients;

}
