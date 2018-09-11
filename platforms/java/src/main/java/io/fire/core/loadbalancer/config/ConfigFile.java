package io.fire.core.loadbalancer.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigFile {

    private Map<String, String> values = new LinkedHashMap<>();

    public ConfigFile() throws IOException {
        //defaults
        values.put("# Config file for the Fire-IO load balancer. Changes will be loaded after a restart \n# Public password, used for clients that are connecting via the load balancer. \n# Private password, used for Fire-IO servers to login to the load balancer.", "");
        values.put("port", "80");
        values.put("public_password", "testpassword1");
        values.put("private_password", "testpassword2");

        values.put("# Ratelimit configuration, x requests per x secons", "");
        values.put("ratelimit_per_session", "10");
        values.put("ratelimit_session_timeout", "5");

        //check if file exists, and based on that, load or create
        File f = new File("server.properties");
        if(f.exists() && !f.isDirectory()) {
            load(new String (Files.readAllBytes(new File("server.properties").toPath()),Charset.forName("UTF-8")));
        } else {
            save();
        }
    }

    public void save() throws IOException {
        FileOutputStream out = new FileOutputStream("server.properties");
        out.write(toString().getBytes());
        out.close();
    }

    public void setString(String key, String value) {
        values.put(key, value);
    }

    public int getInt(String key) {
        return Integer.valueOf(getString(key));
    }
    public String getString(String key) {
        return values.get(key);
    }

    public void load(String data) {
        for (String s : data.split("\n")) {
            if (!s.startsWith("#")) {
                String[] line = s.split(": ");
                values.put(line[0], line[1]);
            }
        }
    }

    @Override
    public String toString() {
        final String[] out = {""};
        values.forEach((ke, va) -> {
            out[0] += ke + ": " + va + "\n";
        });
        return out[0];
    }

}
