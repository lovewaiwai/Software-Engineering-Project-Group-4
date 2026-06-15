package com.swapcampus.user.service.impl;

import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.points.mapper.PointRecordMapper;
import com.swapcampus.user.entity.CreditRecordEntity;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.mapper.CreditRecordMapper;
import com.swapcampus.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceImplTest {

    @Mock private UserMapper userMapper;
    @Mock private CreditRecordMapper creditRecordMapper;
    @Mock private PointRecordMapper pointRecordMapper;

    private UserAccountServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserAccountServiceImpl(userMapper, creditRecordMapper, pointRecordMapper);
    }

    @Test
    void addCreditClampsScoreAndCreatesRecord() {
        UserEntity user = activeUser(7L, 95);
        when(userMapper.selectById(7L)).thenReturn(user);

        int scoreAfter = service.addCredit(7L, 20, "good trade", "ORDER", 99L);

        assertEquals(100, scoreAfter);
        assertEquals(100, user.getCreditScore());
        verify(userMapper).updateById(user);

        ArgumentCaptor<CreditRecordEntity> recordCaptor = ArgumentCaptor.forClass(CreditRecordEntity.class);
        verify(creditRecordMapper).insert(recordCaptor.capture());
        CreditRecordEntity record = recordCaptor.getValue();
        assertEquals(7L, record.getUserId());
        assertEquals(20, record.getDelta());
        assertEquals(100, record.getScoreAfter());
        assertEquals("good trade", record.getReason());
        assertEquals("ORDER", record.getRefType());
        assertEquals(99L, record.getRefId());
        assertNotNull(record.getCreatedAt());
    }

    private UserEntity activeUser(Long id, int creditScore) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreditScore(creditScore);
        user.setPointBalance(0);
        return user;
    }
}
