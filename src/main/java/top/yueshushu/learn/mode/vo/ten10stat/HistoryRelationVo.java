package top.yueshushu.learn.mode.vo.ten10stat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 股票历史展示信息Vo
 * @Author yuejianli
 * @Date 2022/6/5 7:42
 **/
@ApiModel("股票历史展示信息Vo")
@Data
public class HistoryRelationVo implements Serializable {
    @ApiModelProperty("交易日期")
    private String currDate;
    @ApiModelProperty("类型 1赚 -1赔 0是平")
    private Integer type;
    @ApiModelProperty("涨跌比例")
    private String amplitudeProportion;
    @ApiModelProperty("收盘价")
    private String closePrice;
    @ApiModelProperty("是否有标识 0为没有标识 1为有标识")
    private Integer sign = 0;
}
