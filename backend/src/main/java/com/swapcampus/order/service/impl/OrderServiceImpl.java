package com.swapcampus.order.service.impl;

import com.swapcampus.order.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    @Override
    public String moduleName() {
        return "order";
    }
}
