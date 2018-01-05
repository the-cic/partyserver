/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver;

import java.nio.file.Paths;
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
 * @author Mirko Stancic, Dhimahi
 */
public class Config {

    private static final String LOG_CONFIG_FILE = "log4j2.xml";
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "config.ini";

    public static final java.util.logging.Logger DEFAULT_LOGGER = java.util.logging.Logger.getLogger("com.mush.partyserver");

    private final Logger logger;

    private int httpPort;
    private int socketPort;
    private int httpsPort;
    private String keyStorePath;
    private String keyStorePassword;
    private String keyPassword;

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

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
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

        SubnodeConfiguration https = ini.getSection("https");
        httpsPort = https.getInt("port");
        keyStorePath = https.getString("keyStorePath");
        keyStorePassword = https.getString("keyStorePassword");
        keyPassword = https.getString("keyPassword");

        SubnodeConfiguration socket = ini.getSection("websocket");
        socketPort = socket.getInt("port");
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

}
