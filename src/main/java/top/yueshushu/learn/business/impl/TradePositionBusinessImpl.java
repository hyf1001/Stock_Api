package top.yueshushu.learn.business.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.yueshushu.learn.business.TradePositionBusiness;
import top.yueshushu.learn.common.SystemConst;
import top.yueshushu.learn.enumtype.SelectedType;
import top.yueshushu.learn.enumtype.TradeRealValueType;
import top.yueshushu.learn.mode.ro.TradePositionRo;
import top.yueshushu.learn.mode.vo.StockSelectedVo;
import top.yueshushu.learn.mode.vo.TradePositionVo;
import top.yueshushu.learn.response.OutputResult;
import top.yueshushu.learn.service.StockSelectedService;
import top.yueshushu.learn.service.TradePositionService;
import top.yueshushu.learn.service.cache.StockCacheService;
import top.yueshushu.learn.service.cache.TradeCacheService;
import top.yueshushu.learn.util.StockUtil;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 持仓
 *
 * @author Yue Jianli
 * @date 2022-05-26
 */
@Service
@Slf4j
public class TradePositionBusinessImpl implements TradePositionBusiness {
    @Resource
    private TradePositionService tradePositionService;
    @Resource
    private StockCacheService stockCacheService;
    @Resource
    private TradeCacheService tradeCacheService;
    @Resource
    private StockSelectedService stockSelectedService;

    @Override
    public OutputResult mockList(TradePositionRo tradePositionRo) {
        OutputResult <List<TradePositionVo>> outputResult = tradePositionService.mockList(tradePositionRo);
        if (!outputResult.getSuccess()){
            return outputResult;
        }
        return parseFillInfo(outputResult.getData(), tradePositionRo);
    }

    @Override
    public OutputResult realList(TradePositionRo tradePositionRo) {
        if (!tradeCacheService.needSyncReal(TradeRealValueType.TRADE_POSITION, tradePositionRo.getUserId())) {
            return mockList(tradePositionRo);
        }
        log.info(">>>此次员工{}查询需要同步真实的持仓数据",tradePositionRo.getUserId());
        OutputResult<List<TradePositionVo>> outputResult = tradePositionService.realList(tradePositionRo);
        if (!outputResult.getSuccess()){
            return outputResult;
        }
        //获取到最新的持仓信息，更新到相应的数据库中.
        List<TradePositionVo> tradePositionVoList = outputResult.getData();
        // 将数据保存下来
        tradePositionService.syncRealPositionByUserId(tradePositionRo.getUserId(),tradePositionVoList);
        return parseFillInfo(tradePositionVoList, tradePositionRo);
    }

    /**
     * 将获取到的持仓股票信息进行填充，返回
     * @param tradePositionVoList 要解析的数据
     * @param tradePositionRo
     * @return 将获取到的持仓股票信息进行填充，返回
     */
    private OutputResult parseFillInfo(List<TradePositionVo> tradePositionVoList, TradePositionRo tradePositionRo){
        //最后的结果处理
        List<TradePositionVo> result = new ArrayList<>();
        if(!CollectionUtils.isEmpty(tradePositionVoList)){
            //进行获取，补全相关的信息
            for(TradePositionVo tradePositionVo:tradePositionVoList){
                //进行补充数据
                BigDecimal price = stockCacheService.getNowCachePrice(tradePositionVo.getCode());
                tradePositionVo.setPrice(price);
                //设置总的市值
                tradePositionVo.setAllMoney(
                        StockUtil.allMoney(
                                tradePositionVo.getAllAmount(),
                                price
                        )
                );
                //设置浮动盈亏
                tradePositionVo.setFloatMoney(
                        StockUtil.floatMoney(
                                tradePositionVo.getAvgPrice(),
                                price,
                                tradePositionVo.getAllAmount()
                        )
                );
                tradePositionVo.setFloatProportion(
                        StockUtil.floatProportion(
                                tradePositionVo.getAvgPrice(),
                                price,
                                tradePositionVo.getAllAmount()
                        )
                );
                tradePositionVo.setSelectType(SelectedType.POSITION.getCode());
            }
            result.addAll(tradePositionVoList);
        }
        if(!SelectedType.POSITION.getCode().equals(tradePositionRo.getSelectType())){
            //持仓的股票信息
            List<String> positionCodeList = result.stream().map(
                    TradePositionVo::getCode
            ).collect(Collectors.toList());

            //查询该员工对应的自选基金
            List<StockSelectedVo> stockInfoList=
                    stockSelectedService.listSelf(tradePositionRo.getUserId(),
                            null);
            //对自选基金进行处理,如果有的话，就不用处理了。
            if(!CollectionUtils.isEmpty(stockInfoList)){
                for(StockSelectedVo stockSelectedVo:stockInfoList){
                    if(positionCodeList.contains(stockSelectedVo.getStockCode())){
                        continue;
                    }
                    //如果没有的话，就进行相关的查询，组装.
                    TradePositionVo tradePositionVo = getTradePositionVoBySelected(
                            stockSelectedVo,tradePositionRo.getMockType()
                    );
                    result.add(tradePositionVo);
                } }
        }
        return OutputResult.buildSucc(result);
    }

    /**
     * 构建组装成 持仓信息对象
     * @param stockSelectedVo
     * @return
     */
    private TradePositionVo getTradePositionVoBySelected(StockSelectedVo stockSelectedVo,
                                                         int mockType) {
        TradePositionVo tradePositionVo = new TradePositionVo();
        tradePositionVo.setCode(stockSelectedVo.getStockCode());
        tradePositionVo.setName(stockSelectedVo.getStockName());
        tradePositionVo.setAllAmount(0);
        tradePositionVo.setUseAmount(0);
        tradePositionVo.setAvgPrice(
                SystemConst.DEFAULT_EMPTY
        );
        BigDecimal price = stockCacheService.getNowCachePrice(stockSelectedVo.getStockCode());
        tradePositionVo.setPrice(price);
        tradePositionVo.setAllMoney( SystemConst.DEFAULT_EMPTY);
        tradePositionVo.setFloatMoney( SystemConst.DEFAULT_EMPTY);
        tradePositionVo.setFloatProportion(SystemConst.DEFAULT_EMPTY);
        tradePositionVo.setMockType(mockType);
        tradePositionVo.setSelectType(SelectedType.SELECTED.getCode());
        return tradePositionVo;
    }
}