package com.swapcampus.delivery.service.impl;

import com.swapcampus.delivery.service.DeliveryService;
import org.springframework.stereotype.Service;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Override
    public String moduleName() {
        return "delivery";
    }
}
