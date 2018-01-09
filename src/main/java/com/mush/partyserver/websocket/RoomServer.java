/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.websocket;

import com.mush.partyserver.rooms.GuestHandler;
import com.mush.partyserver.rooms.Guest;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 *
 * @author Cic
 */
public class RoomServer extends WebSocketServer {

    private long connectionCount = 0;
    protected Logger logger;
    private GuestHandler handler;
    private Map<WebSocket, Guest> guests;

    public RoomServer(int port, GuestHandler handler0) {
        this(new InetSocketAddress(port));
        logger = LogManager.getLogger(this.getClass());
        logger.info("Created WebSocketServer on port {}", this.getPort());
        guests = new ConcurrentHashMap<>();
        handler = handler0;
    }

    public RoomServer(InetSocketAddress address) {
        super(address);
    }
    
    public GuestHandler getGuestHandler() {
        return handler;
    }
    
    public int getGuestCount() {
        return guests.size();
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake ch) {
        connectionCount++;
        Guest guest = new Guest(connectionCount, ws);
        guests.put(ws, guest);
        try {
            handler.onNewGuest(guest);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void onClose(WebSocket ws, int code, String reason, boolean remote) {
        Guest guest = guests.remove(ws);
        if (guest != null) {
            try {
                handler.onGuestLeft(guest);
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    @Override
    public void onMessage(WebSocket ws, String message) {
        Guest guest = guests.get(ws);
        if (guest != null) {
            logger.info("Message from {} : {}", guest, message);

            try {
                handler.onMessage(message, guest);
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    @Override
    public void onError(WebSocket ws, Exception ex) {
        logger.warn("Error for connection {} : {}", ws, ex);
    }

    @Override
    public void onStart() {
        handler.onStart();
    }

}
