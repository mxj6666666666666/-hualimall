package com.xinjiema.hualimall.mapper;

import com.xinjiema.hualimall.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User selectByUsername(@Param("username") String username);

    User selectById(@Param("id") Long id);

    void insert(User user);

    void insertBuyerProfile(@Param("userId") Long userId, @Param("realName") String realName, @Param("phone") String phone);

    void insertMerchantProfile(@Param("userId") Long userId,
                               @Param("shopName") String shopName,
                               @Param("businessLicenseNo") String businessLicenseNo,
                               @Param("contactName") String contactName,
                               @Param("contactPhone") String contactPhone);

    User selectBuyerProfileByUserId(@Param("userId") Long userId);

    User selectMerchantProfileByUserId(@Param("userId") Long userId);
}
