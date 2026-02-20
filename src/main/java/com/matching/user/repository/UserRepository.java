package com.matching.user.repository;

import com.matching.user.entity.User;
import com.matching.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找
     */
    Optional<User> findByPhone(String phone);

    /**
     * 根据推荐码查找
     */
    Optional<User> findByReferralCode(String referralCode);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);
}
