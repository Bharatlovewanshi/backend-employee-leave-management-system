
package com.leave.admin.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.leave.admin.dto.HolidayRequest;
import com.leave.admin.dto.LeaveRecordResponse;

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

    private <T> T executeRequest(java.util.function.Supplier<T> request) {
        try {
            return request.get();
        } catch (ResourceAccessException ex) {
            throw new IllegalStateException(
                    "Leave Management Service is unavailable. Please try again later.");
        }
    }

    public List<LeaveRecordResponse> getApprovedLeaves() {
        return executeRequest(() -> restClient.get()
                .uri("/api/internal/leaves/approved")
                .retrieve()
                .body(new ParameterizedTypeReference<List<LeaveRecordResponse>>() {}));
    }

    public List<HolidayRequest> getAllHolidays() {
        return executeRequest(() -> restClient.get()
                .uri("/api/holidays")
                .retrieve()
                .body(new ParameterizedTypeReference<List<HolidayRequest>>() {}));
    }

    public HolidayRequest addHoliday(HolidayRequest request) {
        return executeRequest(() -> restClient.post()
                .uri("/api/holidays")
                .body(request)
                .retrieve()
                .body(HolidayRequest.class));
    }

    public HolidayRequest updateHoliday(Long holidayId, HolidayRequest request) {
        return executeRequest(() -> restClient.put()
                .uri("/api/holidays/{holidayId}", holidayId)
                .body(request)
                .retrieve()
                .body(HolidayRequest.class));
    }

    public void deleteHoliday(Long holidayId) {
        executeRequest(() -> {
            restClient.delete()
                    .uri("/api/holidays/{holidayId}", holidayId)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }
}
