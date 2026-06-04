package com.swapcampus.product.service.impl;

import com.swapcampus.product.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Override
    public String moduleName() {
        return "product";
    }
}
