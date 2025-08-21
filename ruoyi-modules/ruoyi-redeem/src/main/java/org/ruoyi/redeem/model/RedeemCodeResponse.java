package org.ruoyi.redeem.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RedeemCodeResponse {
    private String code;
    private String cardNo;
    private BigDecimal amount;
    private BigDecimal userBalance;
    private String msg;
}
