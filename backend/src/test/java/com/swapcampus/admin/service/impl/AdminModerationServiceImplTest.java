package com.swapcampus.admin.service.impl;

import com.swapcampus.admin.dto.ProductReviewRequest;
import com.swapcampus.admin.vo.AdminUserSummaryResponse;
import com.swapcampus.audit.mapper.AuditMapper;
import com.swapcampus.audit.service.AuditLogService;
import com.swapcampus.chat.mapper.ChatMessageMapper;
import com.swapcampus.chat.websocket.ChatWebSocketSessionRegistry;
import com.swapcampus.common.enums.ProductStatus;
import com.swapcampus.common.enums.Role;
import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.order.mapper.OrderMapper;
import com.swapcampus.product.entity.CategoryEntity;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.entity.ProductImageEntity;
import com.swapcampus.product.mapper.CategoryMapper;
import com.swapcampus.product.mapper.ProductImageMapper;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.product.vo.ProductResponse;
import com.swapcampus.report.mapper.ReportActionMapper;
import com.swapcampus.report.mapper.ReportMapper;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.entity.UserProfileEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.mapper.UserProfileMapper;
import com.swapcampus.user.service.UserModerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminModerationServiceImplTest {

    @Mock
    private ReportMapper reportMapper;
    @Mock
    private ReportActionMapper reportActionMapper;
    @Mock
    private ChatMessageMapper chatMessageMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserProfileMapper userProfileMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private AuditMapper auditMapper;
    @Mock
    private UserModerationService userModerationService;
    @Mock
    private ChatWebSocketSessionRegistry sessionRegistry;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private ProductImageMapper productImageMapper;

    private AdminModerationServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminModerationServiceImpl(
                reportMapper,
                reportActionMapper,
                chatMessageMapper,
                userMapper,
                userProfileMapper,
                productMapper,
                orderMapper,
                auditMapper,
                userModerationService,
                sessionRegistry,
                auditLogService,
                categoryMapper,
                productImageMapper
        );
    }

    @Test
    void approveProductActivatesPendingProductAndRecordsAuditLog() {
        ProductEntity product = pendingProduct();
        when(productMapper.selectById(10L)).thenReturn(product);
        when(categoryMapper.selectById(2L)).thenReturn(category());
        when(productImageMapper.selectList(any())).thenReturn(List.of(image("https://cdn.example/1.png")));

        ProductResponse response = adminService.approveProduct(99L, 10L);

        assertEquals(ProductStatus.ACTIVE.name(), product.getStatus());
        assertNull(product.getAuditReason());
        assertEquals(ProductStatus.ACTIVE.name(), response.getStatus());
        assertEquals("Book", response.getCategoryName());
        assertEquals(List.of("FACE_TO_FACE", "LOCKER"), response.getTradeModes());
        assertEquals(List.of("https://cdn.example/1.png"), response.getImageUrls());

        verify(productMapper).updateById(product);
        verify(auditLogService).record(99L, "PRODUCT_APPROVE", "PRODUCT", 10L, "商品审核通过");
    }

    @Test
    void rejectProductStoresReasonAndRecordsAuditLog() {
        ProductEntity product = pendingProduct();
        ProductReviewRequest request = new ProductReviewRequest();
        request.setReason(" missing image ");
        when(productMapper.selectById(10L)).thenReturn(product);
        when(categoryMapper.selectById(2L)).thenReturn(category());
        when(productImageMapper.selectList(any())).thenReturn(List.of());

        ProductResponse response = adminService.rejectProduct(99L, 10L, request);

        assertEquals(ProductStatus.REVIEW_REJECTED.name(), product.getStatus());
        assertEquals("missing image", product.getAuditReason());
        assertEquals("missing image", response.getAuditReason());
        assertEquals(ProductStatus.REVIEW_REJECTED.name(), response.getStatus());

        verify(productMapper).updateById(product);
        verify(auditLogService).record(99L, "PRODUCT_REJECT", "PRODUCT", 10L, "missing image");
    }

    @Test
    void rejectProductRequiresReason() {
        ProductReviewRequest request = new ProductReviewRequest();
        request.setReason("   ");
        when(productMapper.selectById(10L)).thenReturn(pendingProduct());

        BusinessException exception = assertThrows(BusinessException.class, () -> adminService.rejectProduct(99L, 10L, request));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        verify(productMapper, never()).updateById(any(ProductEntity.class));
    }

    @Test
    void approveProductRejectsNonPendingProduct() {
        ProductEntity product = pendingProduct();
        product.setStatus(ProductStatus.ACTIVE.name());
        when(productMapper.selectById(10L)).thenReturn(product);

        BusinessException exception = assertThrows(BusinessException.class, () -> adminService.approveProduct(99L, 10L));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        verify(productMapper, never()).updateById(any(ProductEntity.class));
    }

    @Test
    void banUserRejectsAdminAccounts() {
        UserEntity admin = user(2L, Role.ADMIN, UserStatus.ACTIVE);
        when(userMapper.selectById(2L)).thenReturn(admin);

        BusinessException exception = assertThrows(BusinessException.class, () -> adminService.banUser(99L, 2L, "bad behavior"));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(userMapper, never()).updateById(any(UserEntity.class));
        verify(sessionRegistry, never()).disconnect(any());
    }

    @Test
    void banUserBansNormalUserDisconnectsSessionAndReturnsSummary() {
        UserEntity user = user(2L, Role.USER, UserStatus.ACTIVE);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(2L);
        profile.setRealName("Bob");
        when(userMapper.selectById(2L)).thenReturn(user);
        when(userProfileMapper.selectById(2L)).thenReturn(profile);
        when(userModerationService.getActiveMuteUntil(2L)).thenReturn(null);

        AdminUserSummaryResponse response = adminService.banUser(99L, 2L, "bad behavior");

        assertEquals(UserStatus.BANNED, user.getStatus());
        assertEquals(2L, response.getId());
        assertEquals("Bob", response.getRealName());
        assertEquals(UserStatus.BANNED, response.getStatus());

        verify(userMapper).updateById(user);
        verify(sessionRegistry).disconnect(2L);
        verify(auditLogService).record(eq(99L), eq("USER_BAN"), eq("USER"), eq(2L), anyString());
    }

    private ProductEntity pendingProduct() {
        ProductEntity product = new ProductEntity();
        product.setId(10L);
        product.setSellerId(20L);
        product.setCategoryId(2L);
        product.setTitle("Textbook");
        product.setDescription("Almost new");
        product.setPrice(new BigDecimal("25.00"));
        product.setOriginalPrice(new BigDecimal("50.00"));
        product.setConditionLevel("LIKE_NEW");
        product.setCampus("Main");
        product.setTradeModes("FACE_TO_FACE, LOCKER");
        product.setStatus(ProductStatus.PENDING_REVIEW.name());
        product.setViewCount(0);
        product.setFavoriteCount(0);
        product.setAuditReason("waiting");
        product.setCreatedAt(LocalDateTime.now().minusDays(1));
        return product;
    }

    private CategoryEntity category() {
        CategoryEntity category = new CategoryEntity();
        category.setId(2L);
        category.setName("Book");
        return category;
    }

    private ProductImageEntity image(String url) {
        ProductImageEntity image = new ProductImageEntity();
        image.setId(1L);
        image.setProductId(10L);
        image.setUrl(url);
        image.setSortOrder(1);
        return image;
    }

    private UserEntity user(Long id, Role role, UserStatus status) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername("user-" + id);
        user.setRole(role);
        user.setStatus(status);
        user.setCreditScore(60);
        return user;
    }
}
