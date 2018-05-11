package io.fire.core.client.modules.rest;

import lombok.Setter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class RestModule {

    private String path;
    @Setter private String password = null;

    public RestModule(String host, int port) {
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
            e.printStackTrace();
            return null;
        }
    }

}
