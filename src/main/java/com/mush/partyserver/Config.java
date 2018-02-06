/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Cic
 */
public class Config {

    private static final String LOG_CONFIG_FILE = "log4j2.xml";
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "config.ini";

    public static final java.util.logging.Logger DEFAULT_LOGGER = java.util.logging.Logger.getLogger("com.mush.partyserver");

    private final Logger logger;

    private String rootUri;
    private int httpPort;
    private Integer httpsPort;
    private int socketPort;
    private boolean socketSsl;
    private long loginTimeoutSeconds;
    private long loginTimeoutCheckSeconds;

    private String keyStorePath;
    private String keyStorePassword;
    private String keyPassword;

    private HashMap<String, String> userTokens;
    private HashMap<String, String> tokenUsers;
    private HashMap<String, String> userRooms;

    public Config() {
        logger = setupLog4j();

        readConfig();
    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public HashMap<String, String> getUserTokens() {
        return userTokens;
    }

    public HashMap<String, String> getTokenUsers() {
        return tokenUsers;
    }

    public HashMap<String, String> getUserRooms() {
        return userRooms;
    }

    public boolean getSocketSsl() {
        return socketSsl;
    }

    private Logger setupLog4j() {
        String log4jConfigFile = Paths.get(CONFIG_DIR, LOG_CONFIG_FILE).toString();

        Properties props = System.getProperties();
        props.setProperty("log4j.configurationFile", log4jConfigFile);

        DEFAULT_LOGGER.log(Level.INFO, "Setting log4j.configurationFile to {0}", log4jConfigFile);

        Logger log4j = LogManager.getLogger(this.getClass());
        log4j.info("Log4j2 configured");

        return log4j;
    }

    private void readConfig() {
        INIConfiguration ini = readConfig(CONFIG_FILE);

        SubnodeConfiguration http = ini.getSection("http");
        httpPort = http.getInt("port");
        rootUri = http.getString("rootUri", "html");

        SubnodeConfiguration https = ini.getSection("https");
        httpsPort = https.getInteger("port", null);
        keyStorePath = https.getString("keyStorePath");
        keyStorePassword = https.getString("keyStorePassword");
        keyPassword = https.getString("keyPassword");

        SubnodeConfiguration socket = ini.getSection("websocket");
        socketPort = socket.getInt("port");
        socketSsl = socket.getBoolean("ssl", false);
        loginTimeoutSeconds = socket.getInt("loginTimeoutSeconds", 10);
        loginTimeoutCheckSeconds = socket.getInt("loginTimeoutCheckSeconds", 60);

        userTokens = new HashMap<>();
        tokenUsers = new HashMap<>();
        SubnodeConfiguration users = ini.getSection("users");
        Iterator<String> userKeys = users.getKeys();
        while (userKeys.hasNext()) {
            String key = userKeys.next();
            userTokens.put(key, users.getString(key));
        }
        for (Map.Entry<String, String> e : userTokens.entrySet()) {
            tokenUsers.put(e.getValue(), e.getKey());
        }

        userRooms = new HashMap<>();
        SubnodeConfiguration rooms = ini.getSection("rooms");
        Iterator<String> roomKeys = rooms.getKeys();
        while (roomKeys.hasNext()) {
            String key = roomKeys.next();
            userRooms.put(key, rooms.getString(key));
        }
    }

    private INIConfiguration readConfig(String configFile) {
        String configFilePath = Paths.get(CONFIG_DIR, configFile).toString();
        logger.info("Reading config file: " + configFilePath);

        FileBasedConfigurationBuilder<INIConfiguration> builder
                = new FileBasedConfigurationBuilder<>(INIConfiguration.class)
                        .configure(new Parameters().properties()
                                .setFileName(configFilePath)
                        );

        try {
            return builder.getConfiguration();

        } catch (ConfigurationException | ConversionException ex) {
            logger.error("loading configuration", ex);
        }

        return null;
    }

    /**
     * @return the loginTimeoutSeconds
     */
    public long getLoginTimeoutMillis() {
        return loginTimeoutSeconds * 1000;
    }

    /**
     * @return the loginTimeoutCheckSeconds
     */
    public long getLoginTimeoutCheckMillis() {
        return loginTimeoutCheckSeconds * 1000;
    }

    /**
     * @return the rootUri
     */
    public String getRootUri() {
        return rootUri;
    }

}
