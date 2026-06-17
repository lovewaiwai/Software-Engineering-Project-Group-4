package com.swapcampus.common.moderation;

import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class MockContentModerationService implements ContentModerationService {

    private static final String[] BLOCKED_KEYWORDS = {"违规", "色情", "赌博", "violence"};

    @Override
    public void checkText(String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        String lower = content.toLowerCase();
        for (String keyword : BLOCKED_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "消息包含违规内容");
            }
        }
    }

    @Override
    public void checkImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        if (imageUrl.toLowerCase().contains("blocked") || imageUrl.toLowerCase().contains("violation")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片包含违规内容");
        }
    }
}
