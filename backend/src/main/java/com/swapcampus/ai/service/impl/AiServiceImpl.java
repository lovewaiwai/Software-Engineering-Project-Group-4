package com.swapcampus.ai.service.impl;

import com.swapcampus.ai.service.AiService;
import org.springframework.stereotype.Service;

@Service
public class AiServiceImpl implements AiService {

    @Override
    public String moduleName() {
        return "ai";
    }
}
