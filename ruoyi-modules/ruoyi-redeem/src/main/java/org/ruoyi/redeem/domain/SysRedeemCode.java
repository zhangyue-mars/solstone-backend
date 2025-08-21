package org.ruoyi.redeem.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
@TableName("sys_redeem_code")
public class SysRedeemCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String cardNo;
    private BigDecimal amount;
    private Integer isRedeemed;
    private Long redeemedUserId;
    private Date redeemedTime;
    private String remark;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
    private String delFlag;
    private String tenantId;
}
