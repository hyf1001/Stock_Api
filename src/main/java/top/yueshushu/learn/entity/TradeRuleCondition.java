package top.yueshushu.learn.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 交易规则可使用的条件表
 * </p>
 *
 * @author 两个蝴蝶飞 自定义的
 * @since 2022-01-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TradeRuleCondition implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer id;
    private String code;
    private String name;
    private String description;
    private Date createTime;
    private Date updateTime;
    private Integer flag;
}
