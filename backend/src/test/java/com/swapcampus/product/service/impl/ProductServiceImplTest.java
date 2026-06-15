package com.swapcampus.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swapcampus.common.enums.ProductStatus;
import com.swapcampus.common.enums.Role;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.security.CurrentUserPrincipal;
import com.swapcampus.common.api.PageResponse;
import com.swapcampus.product.dto.ProductImageRequest;
import com.swapcampus.product.dto.ProductRequest;
import com.swapcampus.product.dto.ProductSearchRequest;
import com.swapcampus.product.entity.BrowseRecordEntity;
import com.swapcampus.product.entity.CategoryEntity;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.entity.ProductFavoriteEntity;
import com.swapcampus.product.entity.ProductImageEntity;
import com.swapcampus.product.entity.ProductTagEntity;
import com.swapcampus.product.entity.TagEntity;
import com.swapcampus.product.mapper.BrowseRecordMapper;
import com.swapcampus.product.mapper.CategoryMapper;
import com.swapcampus.product.mapper.ProductFavoriteMapper;
import com.swapcampus.product.mapper.ProductImageMapper;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.product.mapper.ProductTagMapper;
import com.swapcampus.product.mapper.TagMapper;
import com.swapcampus.product.vo.BrowseHistoryResponse;
import com.swapcampus.product.vo.CategoryResponse;
import com.swapcampus.product.vo.ProductResponse;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.service.UserVerificationGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductMapper productMapper;
    @Mock private CategoryMapper categoryMapper;
    @Mock private TagMapper tagMapper;
    @Mock private ProductImageMapper productImageMapper;
    @Mock private ProductFavoriteMapper productFavoriteMapper;
    @Mock private BrowseRecordMapper browseRecordMapper;
    @Mock private ProductTagMapper productTagMapper;
    @Mock private UserMapper userMapper;
    @Mock private UserVerificationGuard userVerificationGuard;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(productMapper, categoryMapper, tagMapper, productImageMapper,
                productFavoriteMapper, browseRecordMapper, productTagMapper, userMapper, userVerificationGuard);
        setCurrentUser(7L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createDraftStoresTrimmedProductImagesAndTags() {
        ProductRequest request = productRequest("DRAFT");
        CategoryEntity category = category(2L, null, "数码");
        TagEntity tag = tag(5L, "耳机");
        when(categoryMapper.selectById(2L)).thenReturn(category);
        when(tagMapper.selectById(5L)).thenReturn(tag);
        doAnswer(invocation -> {
            ProductEntity entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        }).when(productMapper).insert(any(ProductEntity.class));
        stubResponseDependencies(category, List.of(image("https://img/1.png")), List.of(productTag(5L)), List.of(tag), 0L, seller(7L, 88));

        ProductResponse response = service.create(request);

        assertEquals(100L, response.getId());
        assertEquals("优秀", response.getSellerCreditLevel());
        verify(userVerificationGuard).requireVerifiedStudent(7L);
        verify(productImageMapper).insert(any(ProductImageEntity.class));
        verify(productTagMapper).insertTag(100L, 5L);
    }

    @Test
    void createPendingReviewRequiresCompleteFields() {
        ProductRequest request = new ProductRequest();
        request.setStatus("PENDING_REVIEW");

        assertThrows(BusinessException.class, () -> service.create(request));

        verify(productMapper, never()).insert(any(ProductEntity.class));
    }

    @Test
    void listCategoriesBuildsTreeAndListTagsReturnsActiveTags() {
        CategoryEntity root = category(1L, null, "教材");
        CategoryEntity child = category(2L, 1L, "考研");
        when(categoryMapper.selectList(any())).thenReturn(List.of(root, child));
        when(tagMapper.selectList(any())).thenReturn(List.of(tag(9L, "九成新")));

        List<CategoryResponse> categories = service.listCategories();

        assertEquals(1, categories.size());
        assertEquals("教材", categories.get(0).getName());
        assertEquals("考研", categories.get(0).getChildren().get(0).getName());
        assertEquals("九成新", service.listTags().get(0).getName());
    }

    @Test
    void searchMapsPagedActiveProducts() {
        ProductEntity product = product(10L, 8L, ProductStatus.ACTIVE);
        Page<ProductEntity> page = new Page<>(1, 20);
        page.setRecords(List.of(product));
        page.setTotal(1);
        when(productMapper.selectPage(any(), any())).thenReturn(page);
        stubResponseDependencies(category(2L, null, "数码"), List.of(), List.of(), List.of(), 1L, seller(8L, 55));

        ProductSearchRequest request = new ProductSearchRequest();
        request.setKeyword(" 耳机 ");
        request.setCategoryId(2L);
        request.setSort("hot");

        ProductResponse response = service.search(request).getItems().get(0);

        assertEquals(10L, response.getId());
        assertEquals("极差", response.getSellerCreditLevel());
        assertEquals(true, response.getFavorited());
    }

    @Test
    void listBrowseHistoryReturnsLatestViewedProducts() {
        ProductEntity product = product(10L, 8L, ProductStatus.ACTIVE);
        LocalDateTime viewedAt = LocalDateTime.now().minusMinutes(3);
        BrowseRecordEntity record = browseRecord(1L, 7L, 10L, viewedAt);
        when(browseRecordMapper.selectLatestByUser(7L, 0L, 20L)).thenReturn(List.of(record));
        when(browseRecordMapper.countDistinctProductsByUser(7L)).thenReturn(1L);
        when(productMapper.selectById(10L)).thenReturn(product);
        stubResponseDependencies(category(2L, null, "数码"), List.of(), List.of(), List.of(), 0L, seller(8L, 88));

        PageResponse<BrowseHistoryResponse> response = service.listBrowseHistory(1, 20);

        assertEquals(1, response.getTotal());
        assertEquals(10L, response.getItems().get(0).getProduct().getId());
        assertEquals(viewedAt, response.getItems().get(0).getViewedAt());
        assertEquals("优秀", response.getItems().get(0).getProduct().getSellerCreditLevel());
    }

    @Test
    void favoriteInsertsOnlyWhenActiveAndNotAlreadyFavorited() {
        ProductEntity product = product(10L, 8L, ProductStatus.ACTIVE);
        when(productMapper.selectById(10L)).thenReturn(product);
        when(productFavoriteMapper.selectCount(any())).thenReturn(0L);

        assertThrows(com.baomidou.mybatisplus.core.exceptions.MybatisPlusException.class, () -> service.favorite(10L));

        ArgumentCaptor<ProductFavoriteEntity> captor = ArgumentCaptor.forClass(ProductFavoriteEntity.class);
        verify(productFavoriteMapper).insert(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
        assertEquals(10L, captor.getValue().getProductId());
    }

    @Test
    void offlineRelistBoostAndRecordViewUpdateProductState() {
        ProductEntity active = product(10L, 7L, ProductStatus.ACTIVE);
        when(productMapper.selectById(10L)).thenReturn(active);
        stubResponseDependencies(category(2L, null, "数码"), List.of(), List.of(), List.of(), 0L, seller(7L, 96));

        assertEquals(ProductStatus.OFFLINE.name(), service.offline(10L).getStatus());

        active.setStatus(ProductStatus.OFFLINE.name());
        assertEquals(ProductStatus.ACTIVE.name(), service.relist(10L).getStatus());

        active.setStatus(ProductStatus.ACTIVE.name());
        ProductResponse boosted = service.applyBoost(7L, 10L);
        assertNotNull(boosted.getBoostedUntil());

        assertThrows(com.baomidou.mybatisplus.core.exceptions.MybatisPlusException.class, () -> service.recordView(10L));
        verify(browseRecordMapper).insert(any(com.swapcampus.product.entity.BrowseRecordEntity.class));
    }

    @Test
    void addImageRejectsOtherSeller() {
        ProductEntity product = product(10L, 8L, ProductStatus.ACTIVE);
        when(productMapper.selectById(10L)).thenReturn(product);
        ProductImageRequest request = new ProductImageRequest();
        request.setUrl("https://img/2.png");

        assertThrows(BusinessException.class, () -> service.addImage(10L, request));
    }

    @Test
    void creditLevelBoundariesAreStable() {
        assertEquals("极好", service.creditLevel(95));
        assertEquals("优秀", service.creditLevel(85));
        assertEquals("良好", service.creditLevel(60));
        assertEquals("极差", service.creditLevel(59));
        assertFalse(service.toResponse(product(1L, null, ProductStatus.DRAFT), null).getFavorited());
    }

    private void setCurrentUser(Long id) {
        CurrentUserPrincipal principal = new CurrentUserPrincipal(id, "tester", Role.USER);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, null, "ROLE_USER"));
    }

    private ProductRequest productRequest(String status) {
        ProductRequest request = new ProductRequest();
        request.setStatus(status);
        request.setTitle("  蓝牙耳机  ");
        request.setDescription("  几乎全新  ");
        request.setCategoryId(2L);
        request.setPrice(new BigDecimal("99.00"));
        request.setOriginalPrice(new BigDecimal("199.00"));
        request.setConditionLevel(" LIKE_NEW ");
        request.setCampus(" 东校区 ");
        request.setTradeModes(List.of(" MEETUP ", "LOCKER"));
        request.setImageUrls(List.of(" https://img/1.png "));
        request.setTagIds(List.of(5L, 5L));
        return request;
    }

    private ProductEntity product(Long id, Long sellerId, ProductStatus status) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setSellerId(sellerId);
        product.setCategoryId(2L);
        product.setTitle("蓝牙耳机");
        product.setDescription("几乎全新");
        product.setPrice(new BigDecimal("99.00"));
        product.setOriginalPrice(new BigDecimal("199.00"));
        product.setConditionLevel("LIKE_NEW");
        product.setCampus("东校区");
        product.setTradeModes("MEETUP,LOCKER");
        product.setStatus(status.name());
        product.setViewCount(3);
        product.setFavoriteCount(2);
        product.setCreatedAt(LocalDateTime.now().minusDays(1));
        product.setUpdatedAt(LocalDateTime.now());
        product.setDeleted(false);
        return product;
    }

    private CategoryEntity category(Long id, Long parentId, String name) {
        CategoryEntity category = new CategoryEntity();
        category.setId(id);
        category.setParentId(parentId);
        category.setName(name);
        category.setSortOrder(1);
        category.setStatus("ACTIVE");
        return category;
    }

    private TagEntity tag(Long id, String name) {
        TagEntity tag = new TagEntity();
        tag.setId(id);
        tag.setName(name);
        tag.setStatus("ACTIVE");
        return tag;
    }

    private ProductImageEntity image(String url) {
        ProductImageEntity image = new ProductImageEntity();
        image.setId(1L);
        image.setProductId(10L);
        image.setUrl(url);
        image.setSortOrder(0);
        return image;
    }

    private ProductTagEntity productTag(Long tagId) {
        ProductTagEntity productTag = new ProductTagEntity();
        productTag.setProductId(10L);
        productTag.setTagId(tagId);
        return productTag;
    }

    private BrowseRecordEntity browseRecord(Long id, Long userId, Long productId, LocalDateTime viewedAt) {
        BrowseRecordEntity record = new BrowseRecordEntity();
        record.setId(id);
        record.setUserId(userId);
        record.setProductId(productId);
        record.setCategoryId(2L);
        record.setCreatedAt(viewedAt);
        return record;
    }

    private UserEntity seller(Long id, int creditScore) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setCreditScore(creditScore);
        return user;
    }

    private void stubResponseDependencies(CategoryEntity category, List<ProductImageEntity> images,
                                          List<ProductTagEntity> productTags, List<TagEntity> tags,
                                          Long favoriteCount, UserEntity seller) {
        when(categoryMapper.selectById(2L)).thenReturn(category);
        when(productImageMapper.selectList(any())).thenReturn(images);
        when(productTagMapper.selectList(any())).thenReturn(productTags);
        if (!productTags.isEmpty()) {
            when(tagMapper.selectList(any())).thenReturn(tags);
        }
        when(productFavoriteMapper.selectCount(any())).thenReturn(favoriteCount);
        if (seller != null) {
            when(userMapper.selectById(seller.getId())).thenReturn(seller);
        }
    }
}
