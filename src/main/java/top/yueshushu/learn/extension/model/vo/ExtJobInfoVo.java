package top.yueshushu.learn.extension.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 定时任务的展示信息
 * </p>
 *
 * @author 岳建立 自定义的
 * @since 2022-01-02
 */
@Data
@ApiModel("定时任务展示Vo")
public class ExtJobInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id自增")
    private Integer id;

    @ApiModelProperty("任务编码")
    private String code;


    @ApiModelProperty("任务名称")
    private String name;


    @ApiModelProperty("任务描述")
    private String description;

    @ApiModelProperty("任务参数")
    private String param;


    @ApiModelProperty("创建者")
    private String author;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("任务cron表达式")
    private String cron;

    @ApiModelProperty("触发类型 1为手动 0为自动")
    private Integer triggerType;


    @ApiModelProperty("触发状态 1启动中 0为禁止")
    private Integer triggerStatus;

    @ApiModelProperty("上次触发的时间")
    private LocalDateTime triggerLastTime;

    @ApiModelProperty("上次触发的结果 1为正常 0为失败")
    private Integer triggerLastResult;

    @ApiModelProperty("上次触发错误的错误信息")
    private String triggerLastErrorMessage;
    @ApiModelProperty("下次触发的时间")
    private LocalDateTime triggerNextTime;

    @ApiModelProperty("1为正常 0为删除")
    private Integer flag;

}
