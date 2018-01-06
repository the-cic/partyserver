/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms;

/**
 * Message from the client to the server.
 *
 * @author cic
 */
public class ClientMessage extends ServerMessage {

    /**
     * Must include target recipient of message
     */
    public String target;

}
