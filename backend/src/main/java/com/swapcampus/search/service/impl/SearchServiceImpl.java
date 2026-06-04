package com.swapcampus.search.service.impl;

import com.swapcampus.search.service.SearchService;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {

    @Override
    public String moduleName() {
        return "search";
    }
}
