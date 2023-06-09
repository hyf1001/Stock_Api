package top.yueshushu.learn.business.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.yueshushu.learn.business.TradePositionHistoryBusiness;
import top.yueshushu.learn.mode.ro.TradePositionRo;
import top.yueshushu.learn.response.OutputResult;
import top.yueshushu.learn.service.TradePositionHistoryService;
import top.yueshushu.learn.service.cache.StockCacheService;

import javax.annotation.Resource;

/**
 * 持仓历史记录表
 *
 * @author Yue Jianli
 * @date 2022-05-26
 */
@Service
@Slf4j
public class TradePositionHistoryBusinessImpl implements TradePositionHistoryBusiness {
    @Resource
    private TradePositionHistoryService tradePositionHistoryService;
    @Resource
    private StockCacheService stockCacheService;

    @Override
    public OutputResult listHistory(TradePositionRo tradePositionRo) {
        return tradePositionHistoryService.pageHistory(
                tradePositionRo
        );
    }

    @Override
    public OutputResult clearPhCache() {
        stockCacheService.cleanLastTradePositionHistory();
        return OutputResult.buildSucc();
    }
}
