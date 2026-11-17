package com.example.EventGateway.feignClient;

import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.stereotype.Component;

@Component
public class ActuatorHealthStatusProvider implements HealthStatusProvider {
    private final HealthEndpoint healthEndpoint;

    public ActuatorHealthStatusProvider(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @Override
    public String getStatus() {
        return healthEndpoint.health().getStatus().getCode();
    }
}
