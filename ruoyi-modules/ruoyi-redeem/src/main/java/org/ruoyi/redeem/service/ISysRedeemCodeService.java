    
    
package org.ruoyi.redeem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ruoyi.redeem.domain.SysRedeemCode;
import org.ruoyi.redeem.model.AddRedeemCodeRequest;

import org.ruoyi.redeem.model.RedeemCodeRequest;
import org.ruoyi.redeem.model.RedeemCodeResponse;

public interface ISysRedeemCodeService {
    IPage<SysRedeemCode> selectPageList(Page<SysRedeemCode> page, SysRedeemCode query, Integer isRedeemed, String cardNo, String code, java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount);

    RedeemCodeResponse redeemCode(RedeemCodeRequest request);

    String addRedeemCode(AddRedeemCodeRequest request);

    boolean deleteByCode(String code);

    int deleteByCodes(java.util.List<String> codes);
}
