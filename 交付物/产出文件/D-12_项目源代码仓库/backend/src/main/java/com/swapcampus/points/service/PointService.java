package com.swapcampus.points.service;

import com.swapcampus.common.api.PageResponse;
import com.swapcampus.points.dto.PointRedeemRequest;
import com.swapcampus.points.vo.PointItemResponse;
import com.swapcampus.points.vo.PointRedemptionResponse;
import com.swapcampus.points.vo.PointRecordResponse;
import com.swapcampus.points.vo.PointTaskResponse;

import java.util.List;

public interface PointService {

    PointRecordResponse checkIn();

    List<PointTaskResponse> getTasks();

    PointRecordResponse claimTask(String code);

    PageResponse<PointRecordResponse> getRecords(long page, long pageSize);

    List<PointItemResponse> getItems();

    PointRedemptionResponse redeem(PointRedeemRequest request);
}