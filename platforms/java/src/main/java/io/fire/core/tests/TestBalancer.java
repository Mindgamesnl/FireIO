package io.fire.core.tests;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.loadbalancer.startup.BalancerService;

public class TestBalancer {

    public static void main(String[] args) {

        BalancerService.main(new String[] {});
    }

}
