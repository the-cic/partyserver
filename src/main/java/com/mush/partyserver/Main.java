/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver;

import com.mush.partyserver.restlet.ApiApplication;
import com.mush.partyserver.restlet.SecureComponent;
import com.mush.partyserver.rooms.GuestHandler;
import org.restlet.Component;
import org.restlet.resource.ResourceException;
import com.mush.partyserver.websocket.SocketServer;

/**
 *
 * @author Cic
 */
public class Main {

    private static Main instance;

    public final Config config;
    private SocketServer socketServer;

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
        String rootPath = "";

        ApiApplication application = new ApiApplication();

        Component c = new SecureComponent(config);
        c.getDefaultHost().attach(rootPath, application);

        try {
            c.start();
        } catch (Exception ex) {
            throw new ResourceException(ex);
        }
    }

    private void startSocketApi(int port) {
        GuestHandler handler = new GuestHandler(config);
        socketServer = new SocketServer(port, handler);
        socketServer.start();
    }

}
