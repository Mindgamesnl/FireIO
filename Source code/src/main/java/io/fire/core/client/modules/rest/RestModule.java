package io.fire.core.client.modules.rest;

import io.fire.core.client.FireIoClient;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class RestModule {

    private FireIoClient client;
    private String path;
    @Setter private String password = null;

    public RestModule(FireIoClient client, String host, int port) {
        path = "http://" + host + ":" + port + "/";
    }

    public String getToken() {
        try {
            URL website = new URL(path + "fireio/register?p=" + password);
            URLConnection connection = website.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();
            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }

}
