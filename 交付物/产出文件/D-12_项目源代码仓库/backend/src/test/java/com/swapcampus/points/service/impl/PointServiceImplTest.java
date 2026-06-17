package com.swapcampus.points.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swapcampus.audit.service.AuditLogService;
import com.swapcampus.common.enums.ProductStatus;
import com.swapcampus.common.enums.Role;
import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.security.CurrentUserPrincipal;
import com.swapcampus.points.dto.PointRedeemRequest;
import com.swapcampus.points.entity.PointRecordEntity;
import com.swapcampus.points.entity.PointRedemptionEntity;
import com.swapcampus.points.entity.PointTaskEntity;
import com.swapcampus.points.mapper.PointRecordMapper;
import com.swapcampus.points.mapper.PointRedemptionMapper;
import com.swapcampus.points.mapper.PointTaskMapper;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.order.mapper.OrderMapper;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.entity.UserProfileEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.mapper.UserProfileMapper;
import com.swapcampus.user.service.UserAccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @Mock private PointTaskMapper pointTaskMapper;
    @Mock private PointRecordMapper pointRecordMapper;
    @Mock private PointRedemptionMapper pointRedemptionMapper;
    @Mock private UserMapper userMapper;
    @Mock private UserProfileMapper userProfileMapper;
    @Mock private AuditLogService auditLogService;
    @Mock private UserAccountService userAccountService;
    @Mock private ProductMapper productMapper;
    @Mock private OrderMapper orderMapper;

    private PointServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PointServiceImpl(pointTaskMapper, pointRecordMapper, pointRedemptionMapper, userMapper,
                userProfileMapper, auditLogService, userAccountService, productMapper, orderMapper);
        setCurrentUser(7L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void checkInAddsPointsAndRecordsAudit() {
        when(pointTaskMapper.selectOne(any())).thenReturn(task(1L, "DAILY_CHECK_IN", "每日签到", 10, "CHECK_IN"));
        when(userMapper.selectById(7L)).thenReturn(user(90));
        when(pointRecordMapper.selectCount(any())).thenReturn(0L);

        assertEquals(100, service.checkIn().getBalanceAfter());

        verify(userMapper).updateById(any(UserEntity.class));
        verify(pointRecordMapper).insert(any(PointRecordEntity.class));
        verify(auditLogService).record(7L, "POINT_CHECK_IN", "USER", 7L, "每日签到");
    }

    @Test
    void getTasksMarksProfileTaskClaimableOnlyWhenVerifiedProfileComplete() {
        PointTaskEntity checkIn = task(1L, "DAILY_CHECK_IN", "每日签到", 10, "CHECK_IN");
        PointTaskEntity profileTask = task(2L, "PROFILE_COMPLETE", "完善资料", 20, "PROFILE");
        when(pointTaskMapper.selectList(any())).thenReturn(List.of(checkIn, profileTask));
        when(pointRecordMapper.selectCount(any())).thenReturn(0L);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(7L);
        profile.setRealName("Alice");
        profile.setStudentNo("20240001");
        profile.setCollege("CS");
        profile.setVerifiedAt(LocalDateTime.now());
        when(userProfileMapper.selectById(7L)).thenReturn(profile);

        assertEquals(true, service.getTasks().get(1).getClaimable());
    }

    @Test
    void claimProfileTaskRejectsWhenAlreadyClaimed() {
        when(pointTaskMapper.selectOne(any())).thenReturn(task(2L, "PROFILE_COMPLETE", "完善资料", 20, "PROFILE"));
        when(pointRecordMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class, () -> service.claimTask("PROFILE_COMPLETE"));
    }

    @Test
    void getRecordsMapsPagedRecords() {
        Page<PointRecordEntity> page = new Page<>(1, 10);
        PointRecordEntity record = new PointRecordEntity();
        record.setId(1L);
        record.setUserId(7L);
        record.setDelta(10);
        record.setBalanceAfter(110);
        record.setReason("签到");
        record.setCreatedAt(LocalDateTime.now());
        page.setRecords(List.of(record));
        page.setTotal(1);
        when(pointRecordMapper.selectPage(any(), any())).thenReturn(page);

        assertEquals("签到", service.getRecords(1, 10).getItems().get(0).getReason());
    }

    @Test
    void redeemCreditRepairDeductsPointsAndAddsCredit() {
        when(userMapper.selectById(7L)).thenReturn(user(300));
        when(pointRedemptionMapper.selectCount(any())).thenReturn(0L);
        PointRedeemRequest request = new PointRedeemRequest();
        request.setItemCode(" CREDIT_REPAIR ");

        assertEquals(100, service.redeem(request).getBalanceAfter());

        verify(userAccountService).addCredit(7L, 5, "积分兑换：信用分修复", "REDEMPTION", null);
        verify(pointRecordMapper).insert(any(PointRecordEntity.class));
    }

    @Test
    void redeemProductBoostValidatesOwnershipAndUpdatesBoostedUntil() {
        UserEntity user = user(400);
        ProductEntity product = product(10L, 7L, ProductStatus.ACTIVE);
        when(userMapper.selectById(7L)).thenReturn(user);
        when(productMapper.selectCount(any())).thenReturn(1L);
        when(productMapper.selectById(10L)).thenReturn(product);

        PointRedeemRequest request = new PointRedeemRequest();
        request.setItemCode("PRODUCT_BOOST");
        request.setProductId(10L);

        assertEquals(100, service.redeem(request).getBalanceAfter());
        assertNotNull(product.getBoostedUntil());
        verify(productMapper).updateById(product);
    }

    @Test
    void redeemRejectsInsufficientPointsAndMissingProduct() {
        when(userMapper.selectById(7L)).thenReturn(user(100));
        PointRedeemRequest credit = new PointRedeemRequest();
        credit.setItemCode("CREDIT_REPAIR");
        assertThrows(BusinessException.class, () -> service.redeem(credit));

        when(userMapper.selectById(7L)).thenReturn(user(400));
        when(productMapper.selectCount(any())).thenReturn(1L);
        PointRedeemRequest boost = new PointRedeemRequest();
        boost.setItemCode("PRODUCT_BOOST");
        assertThrows(BusinessException.class, () -> service.redeem(boost));
    }

    private void setCurrentUser(Long id) {
        CurrentUserPrincipal principal = new CurrentUserPrincipal(id, "tester", Role.USER);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, null, "ROLE_USER"));
    }

    private PointTaskEntity task(Long id, String code, String name, int points, String type) {
        PointTaskEntity task = new PointTaskEntity();
        task.setId(id);
        task.setCode(code);
        task.setName(name);
        task.setRewardPoints(points);
        task.setTaskType(type);
        task.setStatus("ACTIVE");
        return task;
    }

    private UserEntity user(int points) {
        UserEntity user = new UserEntity();
        user.setId(7L);
        user.setStatus(UserStatus.ACTIVE);
        user.setPointBalance(points);
        return user;
    }

    private ProductEntity product(Long id, Long sellerId, ProductStatus status) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setSellerId(sellerId);
        product.setStatus(status.name());
        product.setDeleted(false);
        return product;
    }
}
