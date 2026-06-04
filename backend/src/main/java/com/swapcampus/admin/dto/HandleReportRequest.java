package com.swapcampus.admin.dto;

import com.swapcampus.common.enums.ReportActionType;
import jakarta.validation.constraints.NotNull;

public class HandleReportRequest {

    @NotNull
    private ReportActionType actionType;

    private String note;

    /** 禁言时长（小时），actionType=MUTE 时使用 */
    private Integer muteHours;

    public ReportActionType getActionType() {
        return actionType;
    }

    public void setActionType(ReportActionType actionType) {
        this.actionType = actionType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getMuteHours() {
        return muteHours;
    }

    public void setMuteHours(Integer muteHours) {
        this.muteHours = muteHours;
    }
}
