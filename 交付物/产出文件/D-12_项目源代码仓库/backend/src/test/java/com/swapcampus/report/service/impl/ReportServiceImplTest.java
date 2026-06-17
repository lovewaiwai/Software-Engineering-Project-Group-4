package com.swapcampus.report.service.impl;

import com.swapcampus.chat.mapper.ChatMessageMapper;
import com.swapcampus.common.enums.ReportStatus;
import com.swapcampus.common.enums.ReportTargetType;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.report.dto.CreateReportRequest;
import com.swapcampus.report.entity.ReportEntity;
import com.swapcampus.report.mapper.ReportMapper;
import com.swapcampus.report.vo.ReportResponse;
import com.swapcampus.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportMapper reportMapper;
    @Mock
    private ChatMessageMapper chatMessageMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ProductMapper productMapper;

    private ReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReportServiceImpl(reportMapper, chatMessageMapper, userMapper, productMapper);
    }

    @Test
    void createProductReportBindsReportedUserToSeller() {
        ProductEntity product = new ProductEntity();
        product.setId(10L);
        product.setSellerId(20L);
        CreateReportRequest request = new CreateReportRequest();
        request.setTargetType(ReportTargetType.PRODUCT);
        request.setTargetId(10L);
        request.setReason("商品信息虚假");
        when(productMapper.selectById(10L)).thenReturn(product);
        when(reportMapper.selectOne(any())).thenReturn(null);

        ReportResponse response = service.createReport(7L, request);

        assertEquals(20L, response.getReportedUserId());
        assertEquals(ReportStatus.PENDING, response.getStatus());
        ArgumentCaptor<ReportEntity> captor = ArgumentCaptor.forClass(ReportEntity.class);
        verify(reportMapper).insert(captor.capture());
        assertEquals(20L, captor.getValue().getReportedUserId());
        assertEquals(ReportTargetType.PRODUCT, captor.getValue().getTargetType());
    }
}
