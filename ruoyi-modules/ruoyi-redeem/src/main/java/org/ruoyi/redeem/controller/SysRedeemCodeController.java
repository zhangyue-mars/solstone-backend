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
            SysRedeemCode query) {
        Page<SysRedeemCode> page = new Page<>(pageNum, pageSize);
        IPage<SysRedeemCode> result = sysRedeemCodeService.selectPageList(page, query);
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

}
