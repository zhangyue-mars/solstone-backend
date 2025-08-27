


    

    

package org.ruoyi.redeem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ruoyi.redeem.domain.SysRedeemCode;
import org.ruoyi.redeem.service.ISysRedeemCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import org.ruoyi.redeem.model.RedeemCodeRequest;
import org.ruoyi.redeem.model.RedeemCodeResponse;
import org.ruoyi.redeem.model.AddRedeemCodeRequest;

@RestController
@RequestMapping("/redeem/code")
public class SysRedeemCodeController {
    @Autowired
    private ISysRedeemCodeService sysRedeemCodeService;

    /**
     * 分页查询兑换码列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer isRedeemed,
            @RequestParam(required = false) String cardNo,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) java.math.BigDecimal minAmount,
            @RequestParam(required = false) java.math.BigDecimal maxAmount) {
        Page<SysRedeemCode> page = new Page<>(pageNum, pageSize);
        IPage<SysRedeemCode> result = sysRedeemCodeService.selectPageList(page, new SysRedeemCode(), isRedeemed, cardNo, code, minAmount, maxAmount);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("msg", "查询成功");
        resp.put("data", result);
        return resp;
    }

    /**
     * 兑换码处理接口
     */
    @PostMapping("/redeem")
    public Map<String, Object> redeem(@RequestBody RedeemCodeRequest request) {
        RedeemCodeResponse data = sysRedeemCodeService.redeemCode(request);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("msg", data.getMsg());
        resp.put("data", data);
        return resp;
    }

    /**
     * 添加兑换码接口
     */
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody AddRedeemCodeRequest request) {
        String code = sysRedeemCodeService.addRedeemCode(request);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("msg", "生成成功");
        resp.put("data", code);
        return resp;
    }


    /**
     * 根据兑换码删除兑换码信息
     */
    // @DeleteMapping("/delete/{code}")
    // public Map<String, Object> deleteByCode(@PathVariable String code) {
    //     boolean success = sysRedeemCodeService.deleteByCode(code);
    //     Map<String, Object> resp = new HashMap<>();
    //     resp.put("code", success ? 200 : 404);
    //     resp.put("msg", success ? "删除成功" : "兑换码不存在");
    //     resp.put("data", null);
    //     return resp;
    // }


    /**
     * 通过参数传递兑换码删除接口（POST方式）
     */
    @PostMapping("/delete")
    public Map<String, Object> deleteByCodeParam(@RequestBody Map<String, String> param) {
        String code = param.get("code");
        boolean success = sysRedeemCodeService.deleteByCode(code);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", success ? 200 : 404);
        resp.put("msg", success ? "删除成功" : "兑换码不存在");
        resp.put("data", null);
        return resp;
    }


        /**
     * 批量删除兑换码接口
     */
    @PostMapping("/deleteBatch")
    public Map<String, Object> deleteBatch(@RequestBody Map<String, java.util.List<String>> param) {
        java.util.List<String> codes = param.get("codes");
        int count = sysRedeemCodeService.deleteByCodes(codes);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("msg", "成功删除" + count + "条");
        resp.put("data", count);
        return resp;
    }

}
