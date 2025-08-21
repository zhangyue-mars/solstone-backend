package org.ruoyi.redeem.model;

import lombok.Data;

@Data
public class RedeemCodeRequest {
    private String code;
    private Long userId;
}
