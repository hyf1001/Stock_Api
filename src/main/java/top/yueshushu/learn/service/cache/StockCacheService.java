package top.yueshushu.learn.service.cache;

import top.yueshushu.learn.entity.User;

import java.math.BigDecimal;

/**
 * @Description 股票的的缓存信息
 * @Author yuejianli
 * @Date 2022/5/20 23:36
 **/
public interface StockCacheService {
    /**
     * 设置股票的当前价格缓存信息
     * @param code 股票代码
     * @param price 股票的价格
     */
    void setNowCachePrice(String code, BigDecimal price);

    /**
     * 获取当前缓存中的股票的当前价格信息
     * @param code 股票代码
     */
    BigDecimal getNowCachePrice(String code);


    /**
     * 设置股票的昨天的收盘价
     * @param code 股票代码
     * @param price 股票的价格
     */
    void setYesterdayCloseCachePrice(String code, BigDecimal price);

    /**
     * 获取缓存中股票的昨天的收盘价
     * @param code 股票代码
     */
    BigDecimal getYesterdayCloseCachePrice(String code);
}