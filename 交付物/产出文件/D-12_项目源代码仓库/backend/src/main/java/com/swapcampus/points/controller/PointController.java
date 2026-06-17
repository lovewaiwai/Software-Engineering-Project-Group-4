package com.swapcampus.points.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.api.PageResponse;
import com.swapcampus.points.dto.PointRedeemRequest;
import com.swapcampus.points.service.PointService;
import com.swapcampus.points.vo.PointItemResponse;
import com.swapcampus.points.vo.PointRedemptionResponse;
import com.swapcampus.points.vo.PointRecordResponse;
import com.swapcampus.points.vo.PointTaskResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @PostMapping("/check-in")
    public ApiResponse<PointRecordResponse> checkIn() {
        return ApiResponse.ok(pointService.checkIn());
    }

    @GetMapping("/tasks")
    public ApiResponse<List<PointTaskResponse>> tasks() {
        return ApiResponse.ok(pointService.getTasks());
    }

    @PostMapping("/tasks/{code}/claim")
    public ApiResponse<PointRecordResponse> claim(@PathVariable String code) {
        return ApiResponse.ok(pointService.claimTask(code));
    }

    @GetMapping("/records")
    public ApiResponse<PageResponse<PointRecordResponse>> records(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(pointService.getRecords(page, pageSize));
    }

    @GetMapping("/items")
    public ApiResponse<List<PointItemResponse>> items() {
        return ApiResponse.ok(pointService.getItems());
    }

    @PostMapping("/redeem")
    public ApiResponse<PointRedemptionResponse> redeem(@Valid @RequestBody PointRedeemRequest request) {
        return ApiResponse.ok(pointService.redeem(request));
    }
}