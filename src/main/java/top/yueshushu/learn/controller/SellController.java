package top.yueshushu.learn.controller;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yueshushu.learn.annotation.AuthToken;
import top.yueshushu.learn.business.SellBusiness;
import top.yueshushu.learn.common.ResultCode;
import top.yueshushu.learn.enumtype.MockType;
import top.yueshushu.learn.mode.ro.SellRo;
import top.yueshushu.learn.response.OutputResult;

import javax.annotation.Resource;

/**
 * <p>
 * 卖出股票处理
 * </p>
 *
 * @author 两个蝴蝶飞
 * @date 2022-01-03
 */
@RestController
@RequestMapping("/sell")
@ApiModel("卖出股票处理")
public class SellController extends BaseController {
    @Resource
    private SellBusiness sellBusiness;
    @PostMapping("/sell")
    @ApiOperation("卖出股票信息")
    @AuthToken
    public OutputResult sell(@RequestBody SellRo sellRo){
        sellRo.setUserId(getUserId());
        if(!StringUtils.hasText(sellRo.getCode())){
            return OutputResult.buildAlert(ResultCode.STOCK_CODE_IS_EMPTY);
        }
        if(sellRo.getAmount() ==null){
            return OutputResult.buildAlert(ResultCode.TOOL_NUMBER_IS_EMPTY);
        }
        if (sellRo.getAmount() % 100 != 0) {
            return OutputResult.buildAlert(
                    ResultCode.TOOL_NUMBER_IS_HUNDREDS
            );
        }
        if (sellRo.getPrice() == null) {
            return OutputResult.buildAlert(ResultCode.TOOL_PRICE_IS_EMPTY);
        }

        if (MockType.MOCK.getCode().equals(sellRo.getMockType())) {
            // 虚拟盘
            return sellBusiness.sell(sellRo);
        }
        return sellBusiness.realSell(sellRo);
    }
}
