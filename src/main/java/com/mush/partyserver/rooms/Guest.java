/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms;

import org.java_websocket.WebSocket;

/**
 *
 * @author cic
 */
public class Guest {

    private String loginName = null;
    private String room = null;
    private boolean isRoomOwner = false;

    private final WebSocket socket;

    public Guest(WebSocket ws) {
        this.socket = ws;
    }

    public void send(String message) {
        this.socket.send(message);
    }

    public void kick() {
        this.socket.close();
    }

    public boolean isLoggedIn() {
        return getLoginName() != null && getRoom() != null;
    }

    void login(String name, String roomName, boolean owner) {
        loginName = name;
        room = roomName;
        isRoomOwner = owner;
    }
    
    void clearRoom() {
        room = null;
    }
    
    @Override
    public String toString(){
        return socket.hashCode() + ":" + getLoginName() + "@" + getRoom();
    }

    /**
     * @return the loginName
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * @return the room
     */
    public String getRoom() {
        return room;
    }

    /**
     * @return the isRoomMaster
     */
    public boolean getIsRoomOwner() {
        return isRoomOwner;
    }
}
