package top.yueshushu.learn.mode.ro;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName:StockStatRo
 * @Description 股票统计所使用的Ro信息
 * @Author 岳建立
 * @Date 2021/11/27 10:27
 * @Version 1.0
 **/
@Data
public class StockBKMoneyStatRo implements Serializable {
    /**
     * @param bkCode 版块的编码
     */
    private String bkCode;
    /**
     * @param startDate 股票的查询开始日期
     */
    private String startDate;
    /**
     * @param endDate 股票的结束日期
     */
    private String endDate;
    /**
     * @param charPriceTypeList 版块资金选择的类型
     */
    private List<String> charBKTypeList;
}