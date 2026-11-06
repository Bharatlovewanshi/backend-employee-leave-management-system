package com.leave.admin.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EmployeeServiceClient {

    private final RestClient restClient;

    public EmployeeServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${employee.service.url}") String employeeServiceUrl) {

        this.restClient = restClientBuilder
                .baseUrl(employeeServiceUrl)
                .build();
    }
}