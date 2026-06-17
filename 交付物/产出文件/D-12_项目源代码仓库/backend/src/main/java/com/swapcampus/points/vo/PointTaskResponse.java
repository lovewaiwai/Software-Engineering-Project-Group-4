package com.swapcampus.points.vo;

import com.swapcampus.points.entity.PointTaskEntity;

public class PointTaskResponse {

    private Long id;
    private String code;
    private String name;
    private Integer rewardPoints;
    private String taskType;
    private String status;
    private Boolean claimed;
    private Boolean claimable;

    public static PointTaskResponse from(PointTaskEntity entity, boolean claimed, boolean claimable) {
        PointTaskResponse response = new PointTaskResponse();
        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setName(entity.getName());
        response.setRewardPoints(entity.getRewardPoints());
        response.setTaskType(entity.getTaskType());
        response.setStatus(entity.getStatus());
        response.setClaimed(claimed);
        response.setClaimable(claimable);
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getClaimed() {
        return claimed;
    }

    public void setClaimed(Boolean claimed) {
        this.claimed = claimed;
    }

    public Boolean getClaimable() {
        return claimable;
    }

    public void setClaimable(Boolean claimable) {
        this.claimable = claimable;
    }
}