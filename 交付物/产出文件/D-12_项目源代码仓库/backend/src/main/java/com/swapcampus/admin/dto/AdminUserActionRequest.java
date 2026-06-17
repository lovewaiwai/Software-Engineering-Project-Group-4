package com.swapcampus.admin.dto;

import jakarta.validation.constraints.Size;

public class AdminUserActionRequest {

    @Size(max = 300)
    private String note;

    private Integer muteHours;

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
