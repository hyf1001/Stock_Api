package top.yueshushu.learn.crawler.service;

import top.yueshushu.learn.mode.ro.StockRo;
import top.yueshushu.learn.response.OutputResult;

import java.util.List;

/**
 * @ClassName:StockHistoryService
 * @Description TODO
 * @Author 岳建立
 * @Date 2021/11/14 11:24
 * @Version 1.0
 **/
public interface CrawlerStockHistoryService {
    /**
     * 股票历史记录同步
     * @param stockRo 股票对象
     * @return 股票历史记录同步
     */
    OutputResult stockCrawlerHistoryAsync(StockRo stockRo);

    /**
     * 股票历史记录同步
     *
     * @param codeList 股票代码对象
     * @return 股票历史记录同步最近一个交易日的信息
     */
    OutputResult easyMoneyYesStockHistory(List<String> codeList);

    /**
     * 股票历史记录同步
     *
     * @param codeList 股票代码对象
     * @return 股票历史记录同步最近一个交易日的信息
     */
    OutputResult txMoneyYesStockHistory(List<String> codeList, List<String> fullCodeList);


    /**
     * 股票历史记录同步
     *
     * @param codeList 股票代码对象
     * @return 股票历史记录同步 今日交易日的信息
     */
    OutputResult txMoneyTodayStockHistory(List<String> codeList, List<String> fullCodeList);

    /**
     * 股票指数信息
     *
     * @param pointCodeList 指数列表
     */
    void syncPointHistory(List<String> pointCodeList);
}
