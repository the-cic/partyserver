/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.websocket;

import com.mush.partyserver.Config;
import com.mush.partyserver.rooms.GuestHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

/**
 *
 * @author cic
 */
public class SecureRoomServer extends RoomServer {

    public SecureRoomServer(int port, GuestHandler handler0) {
        super(port, handler0);
    }

    public void makeSecure(Config config) {
        if (!config.getSocketSsl()) {
            logger.info("Not using secure WebSocket");
            return;
        }
        
        // load up the key store
        String STORETYPE = "JKS";
        String KEYSTORE = config.getKeyStorePath();
        String STOREPASSWORD = config.getKeyStorePassword();
        String KEYPASSWORD = config.getKeyPassword();

        try {

            KeyStore ks = KeyStore.getInstance(STORETYPE);
            File kf = new File(KEYSTORE);
            ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, KEYPASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            this.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
            
            logger.info("Using SSLWebSocketServerFactory");

        } catch (KeyStoreException | IOException | NoSuchAlgorithmException
                | CertificateException | UnrecoverableKeyException
                | KeyManagementException ex) {
            
            logger.error(ex);
        }
    }

}
