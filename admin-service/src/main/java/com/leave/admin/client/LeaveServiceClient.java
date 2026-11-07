package com.leave.admin.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LeaveServiceClient {

    private final RestClient restClient;

    public LeaveServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${leave.service.url}") String leaveServiceUrl) {

        this.restClient = restClientBuilder
                .baseUrl(leaveServiceUrl)
                .build();
    }
}