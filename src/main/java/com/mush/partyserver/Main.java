/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver;

import com.mush.partyserver.restlet.ApiApplication;
import com.mush.partyserver.restlet.SecureComponent;
import com.mush.partyserver.rooms.GuestHandler;
import com.mush.partyserver.websocket.SecureRoomServer;
import org.restlet.Component;
import org.restlet.resource.ResourceException;
import com.mush.partyserver.websocket.RoomServer;
import org.restlet.data.Protocol;

/**
 *
 * @author Cic
 */
public class Main {

    private static Main instance;

    public final Config config;
    private RoomServer socketServer;
    private GuestHandler handler;

    private Main(String[] args) {
        config = new Config();

        try {
            startRestApi();
            startSocketApi(config.getSocketPort());
        } catch (Exception e) {
            System.exit(1);
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        instance = new Main(args);
    }

    private void startRestApi() {
        ApiApplication application = new ApiApplication(config);

        Component c;

        if (config.getHttpsPort() == null) {
            c = new Component();
            c.getServers().add(Protocol.HTTP, config.getHttpPort());
        } else {
            c = new SecureComponent(config);
        }

        c.getClients().add(Protocol.FILE);

        c.getDefaultHost().attach("", application);

        try {
            c.start();
        } catch (Exception ex) {
            throw new ResourceException(ex);
        }
    }

    private void startSocketApi(int port) {
        handler = new GuestHandler(config);
        socketServer = new SecureRoomServer(port, handler);
        ((SecureRoomServer) socketServer).makeSecure(config);
        socketServer.start();
    }

    /**
     * @return the socketServer
     */
    public RoomServer getRoomServer() {
        return socketServer;
    }

}
