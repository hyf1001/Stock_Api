package top.yueshushu.learn.enumtype;

import org.springframework.util.Assert;

/**
 * 展示的价格类型
 * @author 两个蝴蝶飞
 */
public enum CharPriceType {
    /**
     * 开盘价
     */
    OPENINGPRICE(1,"开盘价"),
    /**
     * 收盘价
     */
    CLOSINGPRICE(2,"收盘价"),
    /**
     * 最高价
     */
    HIGHESTPRICE(3,"最高价"),
    /**
     * 最低价
     */
    LOWESTPRICE(4,"最低价"),
    /**
     * 涨幅度
     */
    AMPLITUDEPROPORTION(5,"涨幅度"),
    /**
     * 涨幅金额
     */
    AMPLITUDE(6,"涨幅金额");

    private Integer code;

    private String desc;

    private CharPriceType(Integer code, String desc){
        this.code=code;
        this.desc=desc;
    }

    /**
     * 获取对应的交易所的类型
     * @param code
     * @return
     */
    public static CharPriceType getKType(int code){
        Assert.notNull(code,"code编号不能为空");
        for(CharPriceType exchangeType: CharPriceType.values()){
            if(exchangeType.code.equals(code)){
                return exchangeType;
            }
        }
        return null;
    }
    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}