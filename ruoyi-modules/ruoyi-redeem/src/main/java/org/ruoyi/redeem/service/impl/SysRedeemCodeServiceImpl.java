package org.ruoyi.redeem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ruoyi.redeem.domain.SysRedeemCode;
import org.ruoyi.redeem.mapper.SysRedeemCodeMapper;
import org.ruoyi.redeem.service.ISysRedeemCodeService;
import org.ruoyi.redeem.model.RedeemCodeRequest;
import org.ruoyi.redeem.model.RedeemCodeResponse;
import org.ruoyi.redeem.mapper.SysRedeemCodeCustomMapper;
import org.ruoyi.system.domain.SysUser;
import org.ruoyi.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.ruoyi.redeem.model.AddRedeemCodeRequest;

@Service
public class SysRedeemCodeServiceImpl implements ISysRedeemCodeService {
    @Autowired
    private SysRedeemCodeMapper sysRedeemCodeMapper;
    @Autowired
    private SysRedeemCodeCustomMapper sysRedeemCodeCustomMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public IPage<SysRedeemCode> selectPageList(Page<SysRedeemCode> page, SysRedeemCode query) {
        QueryWrapper<SysRedeemCode> wrapper = new QueryWrapper<>();
        // 可根据需要添加查询条件，如：wrapper.eq("code", query.getCode());
        return sysRedeemCodeMapper.selectPage(page, wrapper);
    }

    @Override
    public RedeemCodeResponse redeemCode(RedeemCodeRequest request) {
        RedeemCodeResponse resp = new RedeemCodeResponse();
        LambdaQueryWrapper<SysRedeemCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRedeemCode::getCode, request.getCode());
        SysRedeemCode redeemCode = sysRedeemCodeMapper.selectOne(wrapper);
        if (redeemCode == null) {
            resp.setMsg("兑换码不存在");
            return resp;
        }
        if (redeemCode.getIsRedeemed() != null && redeemCode.getIsRedeemed() == 1) {
            resp.setMsg("兑换码已兑换");
            resp.setCode(redeemCode.getCode());
            resp.setCardNo(redeemCode.getCardNo());
            resp.setAmount(redeemCode.getAmount());
            return resp;
        }
        // 增加余额
        int update = sysRedeemCodeCustomMapper.addUserBalance(request.getUserId(),
                redeemCode.getAmount().doubleValue());
        if (update > 0) {
            // 标记兑换码为已兑换
            redeemCode.setIsRedeemed(1);
            redeemCode.setRedeemedUserId(request.getUserId());
            redeemCode.setRedeemedTime(new java.util.Date());
            sysRedeemCodeMapper.updateById(redeemCode);
            // 查询用户余额
            SysUser user = sysUserMapper.selectById(request.getUserId());
            resp.setMsg("兑换成功");
            resp.setCode(redeemCode.getCode());
            resp.setCardNo(redeemCode.getCardNo());
            resp.setAmount(redeemCode.getAmount());
            resp.setUserBalance(user != null ? (user.getUserBalance() == null ? null
                    : new java.math.BigDecimal(user.getUserBalance().toString())) : null);
            return resp;
        } else {
            resp.setMsg("用户余额更新失败");
            return resp;
        }
    }

    @Override
    public String addRedeemCode(AddRedeemCodeRequest request) {
        // 生成唯一16位兑换码
        String code;
        int tryCount = 0;
        do {
            code = randomCode(16);
            tryCount++;
            if (tryCount > 10)
                throw new RuntimeException("生成兑换码失败，请重试");
        } while (sysRedeemCodeMapper.selectCount(new QueryWrapper<SysRedeemCode>().eq("code", code)) > 0);

        SysRedeemCode redeemCode = new SysRedeemCode();
        redeemCode.setCode(code);
        redeemCode.setAmount(request.getAmount());
        redeemCode.setIsRedeemed(0);
        redeemCode.setCreateBy(request.getCreateBy());
        redeemCode.setCreateTime(new java.util.Date());
        redeemCode.setCardNo(randomCode(20)); // 自动生成20位卡号
        sysRedeemCodeMapper.insert(redeemCode);
        return code;
    }

    private String randomCode(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = ThreadLocalRandom.current();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
