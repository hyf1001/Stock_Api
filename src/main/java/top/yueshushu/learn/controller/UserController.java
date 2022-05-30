package top.yueshushu.learn.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.yueshushu.learn.business.UserBusiness;
import top.yueshushu.learn.common.ResultCode;
import top.yueshushu.learn.mode.ro.UserRo;
import top.yueshushu.learn.response.OutputResult;
import top.yueshushu.learn.util.SelectConditionUtil;

import javax.annotation.Resource;

/**
 * <p>
 * 登录用户表 我是自定义的
 * </p>
 *
 * @author 岳建立
 * @date 2022-01-02
 */
@RestController
@RequestMapping("/user")
@Api("用户信息")
public class UserController extends BaseController {
    @Resource
    private UserBusiness userBusiness;

    @PostMapping("/login")
    @ApiOperation("用户登录信息")
    public OutputResult login(@RequestBody UserRo userRo) {
        // 对数据进行 check
        if (!StringUtils.hasText(userRo.getAccount())) {
            return OutputResult.buildAlert(ResultCode.ACCOUNT_IS_EMPTY);
        }
        if (!StringUtils.hasText(userRo.getPassword())) {
            return OutputResult.buildAlert(ResultCode.PASSWORD_IS_EMPTY);
        }

        if (SelectConditionUtil.intIsNullOrZero(userRo.getReadAgreement())) {
            return OutputResult.buildAlert(ResultCode.READ_AGREEMENT_TRUE);
        }
        return userBusiness.login(userRo);
    }

    @GetMapping("/convertPassWord")
    @ApiOperation("转换登录用户的密码")
    public OutputResult convertPassWord(String password) {
        return userBusiness.convertPassWord(password);
    }

    @GetMapping("/tradePassword")
    @ApiOperation("转换交易用户的密码")
    public OutputResult tradePassword(String password) {
        return userBusiness.tradePassword(password);
    }

    @GetMapping("/encrypt")
    @ApiOperation("配置文件敏感信息加密")
    public OutputResult encrypt(String text) {
        return userBusiness.encrypt(text);
    }

    @GetMapping("/decrypt")
    @ApiOperation("配置文件敏感信息解密")
    public OutputResult decrypt(String text) {
        return userBusiness.decrypt(text);
    }
}
