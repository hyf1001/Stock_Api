package top.yueshushu.learn.mode.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName:TradeRuleStockQueryDto
 * @Description TODO
 * @Author zk_yjl
 * @Date 2022/1/27 15:03
 * @Version 1.0
 * @Since 1.0
 **/
@Data
@ApiModel("交易股票规则查询")
public class TradeRuleStockQueryDto implements Serializable {
   @ApiModelProperty("用户id")
    private Integer userId;
   @ApiModelProperty("规则id")
    private Integer ruleId;
   @ApiModelProperty("对应的类型")
    private Integer ruleType;
    @ApiModelProperty("是否是模拟的")
    private Integer mockType;
    @ApiModelProperty("股票编码")
    private String stockCode;
}