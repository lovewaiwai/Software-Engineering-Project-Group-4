package com.swapcampus.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swapcampus.common.api.PageResponse;
import com.swapcampus.common.enums.ProductStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.security.CurrentUserContext;
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
import com.swapcampus.product.service.ProductService;
import com.swapcampus.product.vo.BrowseHistoryResponse;
import com.swapcampus.product.vo.CategoryResponse;
import com.swapcampus.product.vo.ProductResponse;
import com.swapcampus.product.vo.TagResponse;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.service.UserVerificationGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final long MAX_PAGE_SIZE = 100;

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductFavoriteMapper productFavoriteMapper;
    private final BrowseRecordMapper browseRecordMapper;
    private final ProductTagMapper productTagMapper;
    private final UserMapper userMapper;
    private final UserVerificationGuard userVerificationGuard;

    public ProductServiceImpl(ProductMapper productMapper,
                              CategoryMapper categoryMapper,
                              TagMapper tagMapper,
                              ProductImageMapper productImageMapper,
                              ProductFavoriteMapper productFavoriteMapper,
                              BrowseRecordMapper browseRecordMapper,
                              ProductTagMapper productTagMapper,
                              UserMapper userMapper,
                              UserVerificationGuard userVerificationGuard) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.productImageMapper = productImageMapper;
        this.productFavoriteMapper = productFavoriteMapper;
        this.browseRecordMapper = browseRecordMapper;
        this.productTagMapper = productTagMapper;
        this.userMapper = userMapper;
        this.userVerificationGuard = userVerificationGuard;
    }

    @Override
    public List<CategoryResponse> listCategories() {
        List<CategoryResponse> nodes = categoryMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
                        .eq(CategoryEntity::getStatus, "ACTIVE")
                        .orderByAsc(CategoryEntity::getSortOrder)
                        .orderByAsc(CategoryEntity::getId))
                .stream()
                .map(this::toCategoryResponse)
                .toList();
        Map<Long, CategoryResponse> byId = nodes.stream()
                .collect(Collectors.toMap(CategoryResponse::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
        List<CategoryResponse> roots = new ArrayList<>();
        for (CategoryResponse node : nodes) {
            if (node.getParentId() == null || !byId.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                byId.get(node.getParentId()).getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    public List<TagResponse> listTags() {
        return tagMapper.selectList(new LambdaQueryWrapper<TagEntity>()
                        .eq(TagEntity::getStatus, "ACTIVE")
                        .orderByAsc(TagEntity::getName))
                .stream()
                .map(this::toTagResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Long sellerId = CurrentUserContext.requireUserId();
        userVerificationGuard.requireVerifiedStudent(sellerId);
        ProductStatus status = createStatus(request);
        if (status == ProductStatus.PENDING_REVIEW) {
            validateRequestReadyForReview(request);
        }
        if (request.getCategoryId() != null) {
            ensureCategoryExists(request.getCategoryId());
        }
        LocalDateTime now = LocalDateTime.now();
        ProductEntity entity = new ProductEntity();
        fillProduct(entity, request);
        entity.setSellerId(sellerId);
        entity.setStatus(status.name());
        entity.setViewCount(0);
        entity.setFavoriteCount(0);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(false);
        productMapper.insert(entity);
        replaceImages(entity.getId(), request.getImageUrls());
        replaceTags(entity.getId(), request.getTagIds());
        return toResponse(entity, sellerId);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        ProductEntity entity = requireProduct(id);
        Long userId = CurrentUserContext.requireUserId();
        userVerificationGuard.requireVerifiedStudent(userId);
        if (!userId.equals(entity.getSellerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能编辑自己发布的商品");
        }
        if (ProductStatus.LOCKED.name().equals(entity.getStatus()) || ProductStatus.SOLD.name().equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "交易中的商品不能编辑");
        }
        ProductStatus status = createStatus(request);
        if (status == ProductStatus.PENDING_REVIEW) {
            validateRequestReadyForReview(request);
        }
        if (request.getCategoryId() != null) {
            ensureCategoryExists(request.getCategoryId());
        }
        fillProduct(entity, request);
        entity.setStatus(status.name());
        entity.setAuditReason(null);
        entity.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(entity);
        replaceImages(entity.getId(), request.getImageUrls());
        replaceTags(entity.getId(), request.getTagIds());
        return toResponse(requireProduct(id), userId);
    }

    @Override
    public ProductResponse detail(Long id) {
        return toResponse(requireProduct(id), CurrentUserContext.currentUserId().orElse(null));
    }

    @Override
    public PageResponse<ProductResponse> search(ProductSearchRequest request) {
        long page = Math.max(1, request.getPage());
        long pageSize = Math.min(MAX_PAGE_SIZE, Math.max(1, request.getPageSize()));
        // 使用 QueryWrapper（非 Lambda）以支持原始 SQL 表达式排序
        QueryWrapper<ProductEntity> wrapper = new QueryWrapper<ProductEntity>()
                .eq("status", ProductStatus.ACTIVE.name());
        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            wrapper.and(w -> w.like("title", keyword)
                    .or()
                    .like("description", keyword));
        }
        if (request.getCategoryId() != null) {
            wrapper.eq("category_id", request.getCategoryId());
        }
        if (request.getMinPrice() != null) {
            wrapper.ge("price", request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            wrapper.le("price", request.getMaxPrice());
        }
        if (StringUtils.hasText(request.getConditionLevel())) {
            wrapper.eq("condition_level", request.getConditionLevel().trim());
        }
        if (StringUtils.hasText(request.getCampus())) {
            wrapper.eq("campus", request.getCampus().trim());
        }
        if (StringUtils.hasText(request.getTradeMode())) {
            wrapper.like("trade_modes", request.getTradeMode().trim());
        }
        // boosted_until > NOW() 的商品排在最前面
        wrapper.orderByAsc("CASE WHEN boosted_until > GETDATE() THEN 0 ELSE 1 END");
        applySort(wrapper, request.getSort());

        IPage<ProductEntity> result = productMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Long userId = CurrentUserContext.currentUserId().orElse(null);
        List<ProductResponse> items = result.getRecords().stream()
                .map(product -> toResponse(product, userId))
                .toList();
        return new PageResponse<>(items, page, pageSize, result.getTotal());
    }

    @Override
    public PageResponse<ProductResponse> listMine(ProductSearchRequest request) {
        Long userId = CurrentUserContext.requireUserId();
        long page = Math.max(1, request.getPage());
        long pageSize = Math.min(MAX_PAGE_SIZE, Math.max(1, request.getPageSize()));
        LambdaQueryWrapper<ProductEntity> wrapper = new LambdaQueryWrapper<ProductEntity>()
                .eq(ProductEntity::getSellerId, userId)
                .orderByDesc(ProductEntity::getCreatedAt);
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(ProductEntity::getStatus, request.getStatus().trim());
        }

        IPage<ProductEntity> result = productMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<ProductResponse> items = result.getRecords().stream()
                .map(product -> toResponse(product, userId))
                .toList();
        return new PageResponse<>(items, page, pageSize, result.getTotal());
    }

    @Override
    public PageResponse<ProductResponse> listFavorites(long page, long pageSize) {
        Long userId = CurrentUserContext.requireUserId();
        long normalizedPage = Math.max(1, page);
        long normalizedPageSize = Math.min(MAX_PAGE_SIZE, Math.max(1, pageSize));
        long offset = (normalizedPage - 1) * normalizedPageSize;
        List<ProductResponse> items = productFavoriteMapper.selectFavoriteProductsByUser(userId, offset, normalizedPageSize)
                .stream()
                .map(product -> toResponse(product, userId))
                .toList();
        long total = Optional.ofNullable(productFavoriteMapper.countFavoriteProductsByUser(userId)).orElse(0L);
        return new PageResponse<>(items, normalizedPage, normalizedPageSize, total);
    }

    @Override
    public PageResponse<BrowseHistoryResponse> listBrowseHistory(long page, long pageSize) {
        Long userId = CurrentUserContext.requireUserId();
        long normalizedPage = Math.max(1, page);
        long normalizedPageSize = Math.min(MAX_PAGE_SIZE, Math.max(1, pageSize));
        long offset = (normalizedPage - 1) * normalizedPageSize;
        List<BrowseRecordEntity> records = browseRecordMapper.selectLatestByUser(userId, offset, normalizedPageSize);
        List<BrowseHistoryResponse> items = records.stream()
                .map(record -> toBrowseHistoryResponse(record, userId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        long total = Optional.ofNullable(browseRecordMapper.countDistinctProductsByUser(userId)).orElse(0L);
        return new PageResponse<>(items, normalizedPage, normalizedPageSize, total);
    }

    @Override
    @Transactional
    public ProductResponse submitForReview(Long id) {
        Long userId = CurrentUserContext.requireUserId();
        userVerificationGuard.requireVerifiedStudent(userId);
        ProductEntity product = requireOwnedProduct(id, userId);
        if (ProductStatus.LOCKED.name().equals(product.getStatus()) || ProductStatus.SOLD.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "交易中的商品不能重新提交审核");
        }
        validateProductReadyForReview(product);
        product.setStatus(ProductStatus.PENDING_REVIEW.name());
        product.setAuditReason(null);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        return toResponse(product, userId);
    }

    @Override
    @Transactional
    public ProductResponse offline(Long id) {
        Long userId = CurrentUserContext.requireUserId();
        ProductEntity product = requireOwnedProduct(id, userId);
        if (!ProductStatus.ACTIVE.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只有已上架商品可以下架");
        }
        product.setStatus(ProductStatus.OFFLINE.name());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        return toResponse(product, userId);
    }

    @Override
    @Transactional
    public ProductResponse relist(Long id) {
        Long userId = CurrentUserContext.requireUserId();
        ProductEntity product = requireOwnedProduct(id, userId);
        if (!ProductStatus.OFFLINE.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只有已下架商品可以重新上架");
        }
        product.setStatus(ProductStatus.ACTIVE.name());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        return toResponse(product, userId);
    }

    @Override
    @Transactional
    public ProductResponse addImage(Long id, ProductImageRequest request) {
        ProductEntity product = requireProduct(id);
        Long userId = CurrentUserContext.requireUserId();
        userVerificationGuard.requireVerifiedStudent(userId);
        if (!userId.equals(product.getSellerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能给自己发布的商品添加图片");
        }
        ProductImageEntity image = new ProductImageEntity();
        image.setProductId(id);
        image.setUrl(request.getUrl());
        image.setSortOrder(Optional.ofNullable(request.getSortOrder()).orElse(0));
        image.setCreatedAt(LocalDateTime.now());
        productImageMapper.insert(image);
        return toResponse(product, userId);
    }

    @Override
    @Transactional
    public void favorite(Long id) {
        ProductEntity product = requireProduct(id);
        if (!ProductStatus.ACTIVE.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能收藏已上架商品");
        }
        Long userId = CurrentUserContext.requireUserId();
        long exists = productFavoriteMapper.selectCount(new LambdaQueryWrapper<ProductFavoriteEntity>()
                .eq(ProductFavoriteEntity::getUserId, userId)
                .eq(ProductFavoriteEntity::getProductId, id));
        if (exists > 0) {
            return;
        }
        ProductFavoriteEntity favorite = new ProductFavoriteEntity();
        favorite.setUserId(userId);
        favorite.setProductId(id);
        favorite.setCreatedAt(LocalDateTime.now());
        productFavoriteMapper.insert(favorite);
        productMapper.update(null, new LambdaUpdateWrapper<ProductEntity>()
                .eq(ProductEntity::getId, id)
                .setSql("favorite_count = favorite_count + 1")
                .set(ProductEntity::getUpdatedAt, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void unfavorite(Long id) {
        Long userId = CurrentUserContext.requireUserId();
        int deleted = productFavoriteMapper.deleteFavorite(userId, id);
        if (deleted > 0) {
            productMapper.update(null, new LambdaUpdateWrapper<ProductEntity>()
                    .eq(ProductEntity::getId, id)
                    .setSql("favorite_count = CASE WHEN favorite_count > 0 THEN favorite_count - 1 ELSE 0 END")
                    .set(ProductEntity::getUpdatedAt, LocalDateTime.now()));
        }
    }

    @Override
    @Transactional
    public void recordView(Long id) {
        ProductEntity product = requireProduct(id);
        Long userId = CurrentUserContext.currentUserId().orElse(null);
        BrowseRecordEntity record = new BrowseRecordEntity();
        record.setUserId(userId);
        record.setProductId(id);
        record.setCategoryId(product.getCategoryId());
        record.setCreatedAt(LocalDateTime.now());
        browseRecordMapper.insert(record);
        productMapper.update(null, new LambdaUpdateWrapper<ProductEntity>()
                .eq(ProductEntity::getId, id)
                .setSql("view_count = view_count + 1")
                .set(ProductEntity::getUpdatedAt, LocalDateTime.now()));
    }

    /**
     * 商品曝光加速：设置 boosted_until 为当前时间 + 24 小时。
     * 由积分兑换模块调用。
     *
     * @param userId    商品所属用户
     * @param productId 要加速的商品 ID
     * @return 加速后的商品响应
     */
    @Transactional
    public ProductResponse applyBoost(Long userId, Long productId) {
        ProductEntity product = requireOwnedProduct(productId, userId);
        if (!ProductStatus.ACTIVE.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只有已上架商品可以加速曝光");
        }
        LocalDateTime boostUntil = LocalDateTime.now().plusHours(24);
        product.setBoostedUntil(boostUntil);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        return toResponse(product, userId);
    }

    private void fillProduct(ProductEntity entity, ProductRequest request) {
        entity.setCategoryId(request.getCategoryId());
        entity.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : null);
        entity.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        entity.setPrice(request.getPrice());
        entity.setOriginalPrice(request.getOriginalPrice());
        entity.setConditionLevel(StringUtils.hasText(request.getConditionLevel()) ? request.getConditionLevel().trim() : null);
        entity.setCampus(StringUtils.hasText(request.getCampus()) ? request.getCampus().trim() : null);
        entity.setTradeModes(toTradeModes(request.getTradeModes()));
    }

    private void validateRequestReadyForReview(ProductRequest request) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写商品标题");
        }
        if (!StringUtils.hasText(request.getDescription())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写商品描述");
        }
        if (request.getCategoryId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择商品分类");
        }
        if (request.getPrice() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写现价");
        }
        if (request.getOriginalPrice() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写原价");
        }
        if (!StringUtils.hasText(request.getConditionLevel())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择成色");
        }
        if (!StringUtils.hasText(request.getCampus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择校区");
        }
        if (request.getTradeModes() == null || request.getTradeModes().stream().noneMatch(StringUtils::hasText)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择交易方式");
        }
        if (request.getImageUrls() == null || request.getImageUrls().stream().noneMatch(StringUtils::hasText)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请至少上传一张商品图片");
        }
        if (request.getTagIds() == null || request.getTagIds().stream().noneMatch(id -> id != null && id > 0)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请至少选择一个商品标签");
        }
    }

    private void validateProductReadyForReview(ProductEntity product) {
        if (!StringUtils.hasText(product.getTitle())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写商品标题");
        }
        if (!StringUtils.hasText(product.getDescription())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写商品描述");
        }
        if (product.getCategoryId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择商品分类");
        }
        ensureCategoryExists(product.getCategoryId());
        if (product.getPrice() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写现价");
        }
        if (product.getOriginalPrice() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写原价");
        }
        if (!StringUtils.hasText(product.getConditionLevel())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择成色");
        }
        if (!StringUtils.hasText(product.getCampus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择校区");
        }
        if (!StringUtils.hasText(product.getTradeModes())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择交易方式");
        }
        if (imageUrls(product.getId()).isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请至少上传一张商品图片");
        }
        if (productTags(product.getId()).isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请至少选择一个商品标签");
        }
    }

    private ProductStatus createStatus(ProductRequest request) {
        if ("DRAFT".equalsIgnoreCase(request.getStatus())) {
            return ProductStatus.DRAFT;
        }
        return ProductStatus.PENDING_REVIEW;
    }

    private String toTradeModes(List<String> tradeModes) {
        if (tradeModes == null || tradeModes.isEmpty()) {
            return "MEETUP";
        }
        return tradeModes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private void replaceImages(Long productId, List<String> imageUrls) {
        productImageMapper.delete(new LambdaQueryWrapper<ProductImageEntity>()
                .eq(ProductImageEntity::getProductId, productId));
        if (imageUrls == null) {
            return;
        }
        int order = 0;
        for (String url : imageUrls) {
            if (!StringUtils.hasText(url)) {
                continue;
            }
            ProductImageEntity image = new ProductImageEntity();
            image.setProductId(productId);
            image.setUrl(url.trim());
            image.setSortOrder(order++);
            image.setCreatedAt(LocalDateTime.now());
            productImageMapper.insert(image);
        }
    }

    private void replaceTags(Long productId, List<Long> tagIds) {
        productTagMapper.deleteByProductId(productId);
        if (tagIds == null) {
            return;
        }
        for (Long tagId : tagIds.stream().filter(id -> id != null && id > 0).distinct().toList()) {
            TagEntity tag = tagMapper.selectById(tagId);
            if (tag == null || !"ACTIVE".equals(tag.getStatus())) {
                continue;
            }
            productTagMapper.insertTag(productId, tagId);
        }
    }

    private ProductEntity requireProduct(Long id) {
        ProductEntity entity = productMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return entity;
    }

    private ProductEntity requireOwnedProduct(Long id, Long userId) {
        ProductEntity product = requireProduct(id);
        if (!userId.equals(product.getSellerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能操作自己发布的商品");
        }
        return product;
    }

    private void ensureCategoryExists(Long categoryId) {
        CategoryEntity category = categoryMapper.selectById(categoryId);
        if (category == null || !"ACTIVE".equals(category.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "商品分类不可用");
        }
    }

    private void applySort(QueryWrapper<ProductEntity> wrapper, String sort) {
        if ("price_asc".equalsIgnoreCase(sort)) {
            wrapper.orderByAsc("price").orderByDesc("created_at");
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            wrapper.orderByDesc("price").orderByDesc("created_at");
        } else if ("hot".equalsIgnoreCase(sort)) {
            wrapper.orderByDesc("view_count").orderByDesc("favorite_count");
        } else {
            wrapper.orderByDesc("created_at");
        }
    }

    public ProductResponse toResponse(ProductEntity entity, Long userId) {
        ProductResponse response = new ProductResponse();
        response.setId(entity.getId());
        response.setSellerId(entity.getSellerId());
        response.setCategoryId(entity.getCategoryId());
        response.setCategoryName(categoryName(entity.getCategoryId()));
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setPrice(entity.getPrice());
        response.setOriginalPrice(entity.getOriginalPrice());
        response.setConditionLevel(entity.getConditionLevel());
        response.setCampus(entity.getCampus());
        response.setTradeModes(splitTradeModes(entity.getTradeModes()));
        response.setStatus(entity.getStatus());
        response.setViewCount(entity.getViewCount());
        response.setFavoriteCount(entity.getFavoriteCount());
        response.setAuditReason(entity.getAuditReason());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setBoostedUntil(entity.getBoostedUntil());
        response.setImageUrls(imageUrls(entity.getId()));
        List<TagEntity> tags = productTags(entity.getId());
        response.setTagIds(tags.stream().map(TagEntity::getId).toList());
        response.setTagNames(tags.stream().map(TagEntity::getName).toList());
        response.setFavorited(isFavorited(userId, entity.getId()));
        applySellerCredit(response, entity.getSellerId());
        return response;
    }

    private String categoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        CategoryEntity category = categoryMapper.selectById(categoryId);
        return category == null ? null : category.getName();
    }

    private List<String> imageUrls(Long productId) {
        return productImageMapper.selectList(new LambdaQueryWrapper<ProductImageEntity>()
                        .eq(ProductImageEntity::getProductId, productId)
                        .orderByAsc(ProductImageEntity::getSortOrder)
                        .orderByAsc(ProductImageEntity::getId))
                .stream()
                .map(ProductImageEntity::getUrl)
                .toList();
    }

    private List<TagEntity> productTags(Long productId) {
        List<Long> tagIds = productTagMapper.selectList(new LambdaQueryWrapper<ProductTagEntity>()
                        .eq(ProductTagEntity::getProductId, productId))
                .stream()
                .map(ProductTagEntity::getTagId)
                .toList();
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return tagMapper.selectList(new LambdaQueryWrapper<TagEntity>()
                .in(TagEntity::getId, tagIds)
                .eq(TagEntity::getStatus, "ACTIVE"));
    }

    private void applySellerCredit(ProductResponse response, Long sellerId) {
        UserEntity seller = sellerId == null ? null : userMapper.selectById(sellerId);
        int score = seller == null || seller.getCreditScore() == null ? 60 : seller.getCreditScore();
        response.setSellerCreditScore(score);
        response.setSellerCreditLevel(creditLevel(score));
    }

    public String creditLevel(int score) {
        if (score >= 95) {
            return "极好";
        }
        if (score >= 85) {
            return "优秀";
        }
        if (score >= 60) {
            return "良好";
        }
        return "极差";
    }

    private Optional<BrowseHistoryResponse> toBrowseHistoryResponse(BrowseRecordEntity record, Long userId) {
        ProductEntity product = productMapper.selectById(record.getProductId());
        if (product == null || Boolean.TRUE.equals(product.getDeleted())) {
            return Optional.empty();
        }
        BrowseHistoryResponse response = new BrowseHistoryResponse();
        response.setProduct(toResponse(product, userId));
        response.setViewedAt(record.getCreatedAt());
        return Optional.of(response);
    }

    private Boolean isFavorited(Long userId, Long productId) {
        if (userId == null) {
            return false;
        }
        return productFavoriteMapper.selectCount(new LambdaQueryWrapper<ProductFavoriteEntity>()
                .eq(ProductFavoriteEntity::getUserId, userId)
                .eq(ProductFavoriteEntity::getProductId, productId)) > 0;
    }

    private List<String> splitTradeModes(String tradeModes) {
        if (!StringUtils.hasText(tradeModes)) {
            return List.of();
        }
        return Arrays.stream(tradeModes.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private CategoryResponse toCategoryResponse(CategoryEntity entity) {
        CategoryResponse response = new CategoryResponse();
        response.setId(entity.getId());
        response.setParentId(entity.getParentId());
        response.setName(entity.getName());
        response.setSortOrder(entity.getSortOrder());
        response.setChildren(new ArrayList<>());
        return response;
    }

    private TagResponse toTagResponse(TagEntity entity) {
        TagResponse response = new TagResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        return response;
    }
}
