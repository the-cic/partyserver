/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.restlet;

import com.mush.partyserver.Config;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.util.Series;

/**
 *
 * @author Cic
 */
public class SecureComponent extends Component {

    /**
     * keytool -genkey -v -alias serverX -dname "CN=**serverX**,OU=IT,O=JPC,C=GB" -keypass password1 -keystore serverX.jks -storepass password2 -keyalg "RSA" -sigalg "MD5withRSA" -keysize 2048 -validity 3650
     *
     * keytool -export -v -alias serverX -file serverX.cer -keystore serverX.jks -storepass password2
     *
     * @param config
     */
    public SecureComponent(Config config) {
        super();

        Server server = getServers().add(Protocol.HTTPS, config.getHttpsPort());

        Series<Parameter> parameters = server.getContext().getParameters();

        parameters.add("sslContextFactory", "org.restlet.engine.ssl.DefaultSslContextFactory");
        parameters.add("keyStorePath", config.getKeyStorePath());
        parameters.add("keyStorePassword", config.getKeyStorePassword());
        parameters.add("keyPassword", config.getKeyPassword());
        parameters.add("keyStoreType", "JKS");
    }

}
