package top.yueshushu.learn.mode.ro;

import lombok.Data;
import lombok.NoArgsConstructor;
import top.yueshushu.learn.response.PageRo;

import java.util.List;

/**
 * @ClassName:StockRo
 * @Description 股票的相关ro
 * @Author 岳建立
 * @Date 2021/11/13 5:50
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
public class StockRo  extends PageRo {
    /**
     * 股票的编码
     */
    private String code;
    /**
     * 查看K线的类型
     */
    private Integer type;

    /**
     * 股票所在的交易所
     */
    private Integer exchange;

    /**
     * 开始日期
     */
    private String startDate;
    /**
     * 结束日期
     */
    private String endDate;
    /**
     * 搜索的关键字
     */
    private String keyword;

    /**
     * 股票编码集合
     */
    private List<String> codes;
}
