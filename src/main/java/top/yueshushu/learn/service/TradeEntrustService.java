package top.yueshushu.learn.service;

import top.yueshushu.learn.entity.TradeEntrust;
import top.yueshushu.learn.enumtype.MockType;
import top.yueshushu.learn.exception.TradeUserException;
import top.yueshushu.learn.mode.ro.TradeEntrustRo;
import top.yueshushu.learn.mode.vo.TradeEntrustVo;
import top.yueshushu.learn.response.OutputResult;
import top.yueshushu.learn.response.PageResponse;

import java.util.List;

/**
 * <p>
 * 委托表 自定义的
 * </p>
 *
 * @author 两个蝴蝶飞
 * @since 2022-01-03
 */
public interface TradeEntrustService {
    /**
     * 获取今日的委托单信息,进行进行中的。
     * @param userId 用户编号
     * @param mockType 股票类型
     * @return 获取今日的委托单信息,进行进行中的。
     */
    List<TradeEntrust> listNowRunEntrust(Integer userId, Integer mockType);

    /**
     * 查询虚拟的今日委托信息
     *
     * @param tradeEntrustRo 委托对象
     * @return 查询虚拟的今日委托信息
     */
    OutputResult<PageResponse<TradeEntrustVo>> mockList(TradeEntrustRo tradeEntrustRo);

    /**
     * 查询真实的今日委托信息
     *
     * @param tradeEntrustRo 委托对象
     * @return 查询真实的今日委托信息
     */
    OutputResult<List<TradeEntrustVo>> realList(TradeEntrustRo tradeEntrustRo) throws TradeUserException;

    /**
     * 同步今日委托信息
     * @param userId 用户编号
     * @param tradePositionVoList 要同步的今日委托对象
     */
    void syncRealEntrustByUserId(Integer userId, List<TradeEntrustVo> tradePositionVoList);

    /**
     * 查询虚拟的历史委托信息
     *
     * @param tradeEntrustRo 委托对象
     * @return 查询虚拟的历史委托信息
     */
    OutputResult<PageResponse<TradeEntrustVo>> mockHistoryList(TradeEntrustRo tradeEntrustRo);

    /**
     * 查询真实的历史委托信息
     *
     * @param tradeEntrustRo 委托对象
     * @return 查询真实的历史委托信息
     */
    List<TradeEntrustVo> realHistoryList(TradeEntrustRo tradeEntrustRo) throws TradeUserException;

    /**
     * 同步东方财富委托信息到数据库
     *
     * @param userId   用户编号
     * @param mockType 类型
     */
    void syncEasyMoneyToDB(Integer userId, MockType mockType);

    /**
     * 根据条件，查询委托单信息
     *
     * @param tradeEntrustRo 查询委托单条件
     */
    OutputResult<TradeEntrustVo> getInfoByCondition(TradeEntrustRo tradeEntrustRo);
}
