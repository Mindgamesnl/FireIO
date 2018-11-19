package io.fire.core.loadbalancer.startup;

import io.fire.core.loadbalancer.FireIoBalancer;
import io.fire.core.loadbalancer.config.ConfigFile;

import java.io.IOException;

public class BalancerService {


    /**
     * Main method.
     * Cuz java has royal autism and wants it to be static.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            ConfigFile config = new ConfigFile();
            new FireIoBalancer(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
