package com.swapcampus.common.moderation;

public interface ContentModerationService {

    void checkText(String content);

    void checkImageUrl(String imageUrl);
}
