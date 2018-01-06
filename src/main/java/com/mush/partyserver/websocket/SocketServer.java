/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.websocket;

import com.mush.partyserver.rooms.GuestHandler;
import com.mush.partyserver.rooms.Guest;
import java.net.InetSocketAddress;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 *
 * @author Cic
 */
public class SocketServer extends WebSocketServer {

    private int connectionCount = 0;
    private Logger logger;
    private GuestHandler handler;
    private HashMap<Integer, Guest> guests;

    public SocketServer(int port) {
        this(new InetSocketAddress(port));
        logger = LogManager.getLogger(this.getClass());
        logger.info("Created WebSocketServer on port {}", this.getPort());
        guests = new HashMap<>();
        handler = new GuestHandler();
    }

    public SocketServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake ch) {
        connectionCount ++;
        Guest guest = new Guest(ws);
        guests.put(ws.hashCode(), guest);
        handler.onNewGuest(guest);
//        logger.info("open {}", ws.hashCode());
    }

    @Override
    public void onClose(WebSocket ws, int code, String reason, boolean remote) {
        connectionCount --;
        Guest guest = guests.get(ws.hashCode());
        guests.remove(ws.hashCode());
        handler.onGuestLeft(guest);
//        logger.info("close {} : {} : {}", ws.hashCode(), code, reason);
    }

    @Override
    public void onMessage(WebSocket ws, String message) {
        Guest guest = guests.get(ws.hashCode());
        logger.info("message {} g:{} : {}", ws.hashCode(), guest, message);
        
        handler.onMessage(message, guest);
    }

    @Override
    public void onError(WebSocket ws, Exception ex) {
        logger.warn("Error for connection {} : {}", ws, ex);
    }

    @Override
    public void onStart() {
//        logger.info("start");
    }

}
