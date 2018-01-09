/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;

/**
 *
 * @author cic
 */
public class Guest {

    private String loginName = null;
    private String room = null;
    private boolean isRoomOwner = false;

    private final long id;
    private final WebSocket socket;
    private final long connectedAt;

    public Guest(long connectionId, WebSocket ws) {
        this.id = connectionId;
        this.socket = ws;
        this.connectedAt = System.currentTimeMillis();
    }

    public void send(String message) {
        this.socket.send(message);
    }

    public void kick(String message) {
        this.socket.close(CloseFrame.NORMAL, message);
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
        return id + ":" + getLoginName() + "@" + getRoom();
    }

    public long getConnectionDuration(){
        return System.currentTimeMillis() - connectedAt;
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

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }
}
