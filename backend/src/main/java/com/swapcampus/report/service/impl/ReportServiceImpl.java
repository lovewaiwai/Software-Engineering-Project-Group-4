package com.swapcampus.report.service.impl;

import com.swapcampus.report.service.ReportService;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl implements ReportService {

    @Override
    public String moduleName() {
        return "report";
    }
}
