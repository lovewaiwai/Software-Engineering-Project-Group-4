package com.swapcampus.ai.service;

import com.swapcampus.ai.dto.AiRequest;
import com.swapcampus.ai.vo.AiResponse;

public interface AiService {

    String moduleName();

    AiResponse suggestProduct(AiRequest request);
}
