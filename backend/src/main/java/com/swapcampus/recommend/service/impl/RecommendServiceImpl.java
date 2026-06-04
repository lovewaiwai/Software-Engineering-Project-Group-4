package com.swapcampus.recommend.service.impl;

import com.swapcampus.recommend.service.RecommendService;
import org.springframework.stereotype.Service;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Override
    public String moduleName() {
        return "recommend";
    }
}
