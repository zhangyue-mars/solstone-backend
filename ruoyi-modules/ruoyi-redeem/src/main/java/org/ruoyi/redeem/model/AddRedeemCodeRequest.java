package org.ruoyi.redeem.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddRedeemCodeRequest {
    private BigDecimal amount;
    private String createBy;
}
