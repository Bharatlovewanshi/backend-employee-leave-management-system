
package com.leave.admin.service;

import java.util.List;

import com.leave.admin.dto.ManagerRequest;
import com.leave.admin.dto.ManagerResponse;

public interface ManagerManagementService {

    ManagerResponse createManager(ManagerRequest request);

    List<ManagerResponse> getAllManagers();

    ManagerResponse getManagerById(Long managerId);

    ManagerResponse updateManager(Long managerId, ManagerRequest request);

    void deleteManager(Long managerId);
}
