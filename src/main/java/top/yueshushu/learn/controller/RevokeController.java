package top.yueshushu.learn.controller;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yueshushu.learn.annotation.AuthToken;
import top.yueshushu.learn.business.RevokeBusiness;
import top.yueshushu.learn.common.ResultCode;
import top.yueshushu.learn.enumtype.MockType;
import top.yueshushu.learn.mode.ro.RevokeRo;
import top.yueshushu.learn.response.OutputResult;

import javax.annotation.Resource;

/**
 * <p>
 * 撤销委托单
 * </p>
 *
 * @author 两个蝴蝶飞
 * @date 2022-01-03
 */
@RestController
@RequestMapping("/revoke")
@ApiModel("撤销委托单")
public class RevokeController extends BaseController {
    @Resource
    private RevokeBusiness revokeBusiness;
    @PostMapping("/revoke")
    @ApiOperation("撤销委托信息")
    @AuthToken
    public OutputResult revoke(@RequestBody RevokeRo revokeRo) {
        revokeRo.setUserId(getUserId());
        if (null == revokeRo.getId() && StringUtils.isBlank(revokeRo.getCode())) {
            return OutputResult.buildFail(ResultCode.ID_IS_EMPTY);
        }
        if (MockType.MOCK.getCode().equals(revokeRo.getMockType())) {
            // 虚拟盘
            return revokeBusiness.revoke(revokeRo);
        }
        return revokeBusiness.realRevoke(revokeRo);

    }
}
