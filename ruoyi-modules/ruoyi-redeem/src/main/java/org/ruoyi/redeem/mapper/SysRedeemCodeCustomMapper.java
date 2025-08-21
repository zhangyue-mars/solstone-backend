package org.ruoyi.redeem.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysRedeemCodeCustomMapper {
    @Update("UPDATE sys_user SET user_balance = user_balance + #{amount} WHERE user_id = #{userId}")
    int addUserBalance(@Param("userId") Long userId, @Param("amount") Double amount);
}
