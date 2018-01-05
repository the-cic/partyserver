/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package websocket;

import java.net.InetSocketAddress;
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

    public SocketServer(int port) {
        this(new InetSocketAddress(port));
        logger = LogManager.getLogger(this.getClass());
        logger.info("Created WebSocketServer on port {}", this.getPort());
    }

    public SocketServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake ch) {
        logger.info("open");
    }

    @Override
    public void onClose(WebSocket ws, int i, String string, boolean bln) {
        logger.info("close");
    }

    @Override
    public void onMessage(WebSocket ws, String string) {
        logger.info("message");
    }

    @Override
    public void onError(WebSocket ws, Exception excptn) {
        logger.info("error");
    }

    @Override
    public void onStart() {
        logger.info("start");
    }

}
