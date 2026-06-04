package com.swapcampus.review.service.impl;

import com.swapcampus.review.service.ReviewService;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Override
    public String moduleName() {
        return "review";
    }
}
