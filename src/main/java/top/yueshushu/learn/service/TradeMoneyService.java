package top.yueshushu.learn.service;

import top.yueshushu.learn.entity.TradeMoney;
import top.yueshushu.learn.mode.ro.TradeMoneyRo;
import top.yueshushu.learn.mode.vo.TradeMoneyVo;
import top.yueshushu.learn.response.OutputResult;

/**
 * <p>
 * 资金表 自定义的
 * </p>
 *
 * @author 两个蝴蝶飞
 * @since 2022-01-03
 */
public interface TradeMoneyService{

    /**
     * 更新用户的资产信息
     * @param tradeMoney 携带着id的 money信息
     */
    void updateMoney(TradeMoney tradeMoney);

    /**
     * 获取用户的资产信息
     * @param userId 用户编号
     * @param mockType 股票类型
     * @return 获取用户的资产信息
     */
    TradeMoney getByUserIdAndMockType(Integer userId, Integer mockType);

    /**
     * 查询虚拟的股票当前的持仓记录信息
     * @param tradeMoneyRo 股票持仓对象
     * @return 查询虚拟的股票当前的持仓记录信息
     */
    OutputResult <TradeMoneyVo> mockInfo(TradeMoneyRo tradeMoneyRo);
    /**
     * 查询真实的股票当前的持仓记录信息
     * @param tradeMoneyRo 股票持仓对象
     * @return 查询真实的股票当前的持仓记录信息
     */
    OutputResult<TradeMoneyVo> realInfo(TradeMoneyRo tradeMoneyRo);
}