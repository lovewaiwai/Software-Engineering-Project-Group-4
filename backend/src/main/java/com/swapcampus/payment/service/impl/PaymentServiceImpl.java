package com.swapcampus.payment.service.impl;

import com.swapcampus.payment.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Override
    public String moduleName() {
        return "payment";
    }
}
