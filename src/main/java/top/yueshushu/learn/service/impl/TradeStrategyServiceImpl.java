package top.yueshushu.learn.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.yueshushu.learn.business.BuyBusiness;
import top.yueshushu.learn.business.SellBusiness;
import top.yueshushu.learn.domainservice.StockSelectedDomainService;
import top.yueshushu.learn.entity.Stock;
import top.yueshushu.learn.enumtype.ConfigCodeType;
import top.yueshushu.learn.enumtype.EntrustType;
import top.yueshushu.learn.mode.ro.BuyRo;
import top.yueshushu.learn.mode.ro.SellRo;
import top.yueshushu.learn.mode.vo.ConfigVo;
import top.yueshushu.learn.service.ConfigService;
import top.yueshushu.learn.service.StockService;
import top.yueshushu.learn.service.TradeStrategyService;
import top.yueshushu.learn.util.BigDecimalUtil;
import top.yueshushu.learn.util.StockRedisUtil;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName:TradeStrategyServiceImpl
 * @Description TODO
 * @Author zk_yjl
 * @Date 2022/1/11 20:33
 * @Version 1.0
 * @Since 1.0
 **/
@Service
@Slf4j
public class TradeStrategyServiceImpl implements TradeStrategyService {
    @Resource
    private StockSelectedDomainService stockSelectedDomainService;
    @Resource
    private BuyBusiness buyBusiness;
    @Resource
    private SellBusiness sellBusiness;
    @Resource
    private StockRedisUtil stockRedisUtil;
    @Resource
    private ConfigService configService;
    @Resource
    private StockService stockService;
    @Override
    public void mockEntructXxlJob(BuyRo buyRo) {
        // 查询虚拟的买入差值价
        ConfigVo buyPriceVo = configService.getConfig(buyRo.getUserId(), ConfigCodeType.MOCK_BUY_SUB_PRICE);
        BigDecimal buySubPrice = BigDecimalUtil.toBigDecimal(buyPriceVo.getCodeValue());

        ConfigVo sellPriceVo = configService.getConfig(buyRo.getUserId(), ConfigCodeType.MOCK_SELL_SUB_PRICE);
        BigDecimal sellSubPrice = BigDecimalUtil.toBigDecimal(sellPriceVo.getCodeValue());


        List<String> codeList = stockSelectedDomainService.findCodeList(null);
        //查询该员工最开始的收盘价
        for(String code:codeList){
            //获取昨天的价格
            BigDecimal yesPrice = stockRedisUtil.getYesPrice(code);
            //获取今天的价格
            BigDecimal currentPrice = stockRedisUtil.getPrice(code);
            //查询当前股票的名称
            Stock stock = stockService.selectByCode(code);
            //+ 相差 2元，就    110  2    --->  108 107
            if(BigDecimalUtil.subBigDecimal(yesPrice, buySubPrice).compareTo(currentPrice)>0){
                //可以买入
                BuyRo mockBuyRo = new BuyRo();
                mockBuyRo.setUserId(buyRo.getUserId());
                mockBuyRo.setMockType(buyRo.getMockType());
                mockBuyRo.setCode(code);
                mockBuyRo.setAmount(100);
                mockBuyRo.setName(stock.getName());
                mockBuyRo.setPrice(
                        BigDecimalUtil.subBigDecimal(
                                yesPrice,
                                buySubPrice
                        )
                );
                log.info(">>>可以买入股票{}",code);
                mockBuyRo.setEntrustType(EntrustType.AUTO.getCode());
                buyBusiness.buy(mockBuyRo);
            }

            if(BigDecimalUtil.subBigDecimal(
                    currentPrice,
                    sellSubPrice
            ).compareTo(yesPrice)>=0){
                //开始买
                SellRo sellRo = new SellRo();
                sellRo.setUserId(buyRo.getUserId());
                sellRo.setMockType(buyRo.getMockType());
                sellRo.setCode(code);
                sellRo.setAmount(100);
                sellRo.setName(stock.getName());
                sellRo.setPrice(
                        BigDecimalUtil.addBigDecimal(yesPrice, sellSubPrice
                        )
                );
                sellRo.setEntrustType(EntrustType.AUTO.getCode());
                log.info(">>>可以卖出股票{}",code);
                sellBusiness.sell(sellRo);
            }
        }
    }
}