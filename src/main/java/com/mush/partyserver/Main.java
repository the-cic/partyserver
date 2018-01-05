/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver;

import com.mush.partyserver.restlet.ApiApplication;
import com.mush.partyserver.restlet.SecureComponent;
import java.io.IOException;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.resource.ResourceException;
import websocket.SocketServer;

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

//        Component c = new Component();
//        c.getServers().add(Protocol.HTTP, port);
//        c.getDefaultHost().attach(rootPath, application);

        Component c = new SecureComponent(config);
        c.getDefaultHost().attach(rootPath, application);

        try {
            c.start();
        } catch (Exception ex) {
            throw new ResourceException(ex);
        }
    }

    private void startSocketApi(int port) {
        socketServer = new SocketServer(port);
    }

}
