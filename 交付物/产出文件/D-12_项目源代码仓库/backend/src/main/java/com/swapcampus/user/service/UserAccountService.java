package com.swapcampus.user.service;

/**
 * 用户账户公共服务接口——供各模块跨业务调整信用分和积分，并自动写入对应流水表。
 * <p>
 * 调用方无需关心流水记录的创建逻辑，只需传入变更原因和业务引用即可。
 * 所有方法均为事务操作，保证分值与流水的一致性。
 * </p>
 */
public interface UserAccountService {

    /**
     * 调整用户信用分并写入 credit_records 流水。
     *
     * @param userId  目标用户 ID
     * @param delta   变更值（正数加分，负数扣分）
     * @param reason  变更原因（用于 credit_records.reason）
     * @param refType 业务引用类型（用于 credit_records.ref_type），可为 null
     * @param refId   业务引用 ID（用于 credit_records.ref_id），可为 null
     * @return 调整后的信用分
     */
    int addCredit(Long userId, int delta, String reason, String refType, Long refId);

    /**
     * 调整用户积分并写入 point_records 流水。
     *
     * @param userId  目标用户 ID
     * @param delta   变更值（正数加分，负数扣分）
     * @param reason  变更原因（用于 point_records.reason）
     * @param refType 业务引用类型（用于 point_records.ref_type），可为 null
     * @param refId   业务引用 ID（用于 point_records.ref_id），可为 null
     * @return 调整后的积分余额
     */
    int addPoints(Long userId, int delta, String reason, String refType, Long refId);
}