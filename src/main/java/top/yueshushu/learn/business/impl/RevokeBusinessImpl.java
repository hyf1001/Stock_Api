package top.yueshushu.learn.business.impl;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.yueshushu.learn.api.TradeResultVo;
import top.yueshushu.learn.api.request.RevokeRequest;
import top.yueshushu.learn.api.response.RevokeResponse;
import top.yueshushu.learn.business.RevokeBusiness;
import top.yueshushu.learn.common.ResultCode;
import top.yueshushu.learn.domain.TradeEntrustDo;
import top.yueshushu.learn.domainservice.TradeEntrustDomainService;
import top.yueshushu.learn.entity.TradeMoney;
import top.yueshushu.learn.entity.TradePosition;
import top.yueshushu.learn.enumtype.DealType;
import top.yueshushu.learn.enumtype.EntrustStatusType;
import top.yueshushu.learn.enumtype.EntrustType;
import top.yueshushu.learn.helper.TradeRequestHelper;
import top.yueshushu.learn.mode.ro.RevokeRo;
import top.yueshushu.learn.response.OutputResult;
import top.yueshushu.learn.service.TradeMoneyService;
import top.yueshushu.learn.service.TradePositionService;
import top.yueshushu.learn.util.BigDecimalUtil;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Description 委托撤销
 * @Author yuejianli
 * @Date 2022/5/28 21:50
 **/
@Slf4j
@Service

public class RevokeBusinessImpl implements RevokeBusiness {

    @Resource
    private TradeMoneyService tradeMoneyService;
    @Resource
    private TradePositionService tradePositionService;
    @Resource
    private TradeEntrustDomainService tradeEntrustDomainService;
    @Resource
    private TradeRequestHelper tradeRequestHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OutputResult revoke(RevokeRo revokeRo) {
        //查询单号信息
        TradeEntrustDo tradeEntrustDo = tradeEntrustDomainService.getById(revokeRo.getId());
        if (null == tradeEntrustDo) {
            return OutputResult.buildAlert(ResultCode.TRADE_ENTRUST_ID_EMPTY);
        }
        if (!tradeEntrustDo.getUserId().equals(revokeRo.getUserId())) {
            return OutputResult.buildAlert(ResultCode.NO_AUTH);
        }
        if (!EntrustStatusType.ING.getCode().equals(tradeEntrustDo.getEntrustStatus())) {
            return OutputResult.buildAlert(ResultCode.TRADE_ENTRUST_STATUS_ERROR);
        }
        //获取委托的类型
        Integer dealType = tradeEntrustDo.getDealType();
        if (EntrustType.AUTO.getCode().equals(revokeRo.getEntrustType())) {
            tradeEntrustDo.setEntrustStatus(EntrustStatusType.AUTO_REVOKE.getCode());
        } else {
            tradeEntrustDo.setEntrustStatus(EntrustStatusType.HAND_REVOKE.getCode());
        }
        if (DealType.BUY.getCode().equals(dealType)) {
            return buyRevoke(tradeEntrustDo);
        } else {
            return sellRevoke(tradeEntrustDo);
        }
    }

    @Override
    public OutputResult realRevoke(RevokeRo revokeRo) {
        RevokeRequest request = new RevokeRequest(revokeRo.getUserId());
        String revokes = String.format("%s_%s", DateUtil.format(new Date(), "yyyyMMdd"), revokeRo.getCode());
        request.setRevokes(revokes);
        TradeResultVo<RevokeResponse> response = tradeRequestHelper.sendRealRevokeReq(request);
        if (!response.getSuccess()) {
            return OutputResult.buildFail(ResultCode.REAL_REVOKE_ERROR);
        }
        return OutputResult.buildSucc(response.getMessage());
    }

    private OutputResult sellRevoke(TradeEntrustDo tradeEntrustDo) {
        //更新
        tradeEntrustDomainService.updateById(tradeEntrustDo);

        TradePosition tradePosition = tradePositionService.getPositionByCode(
                tradeEntrustDo.getUserId(),
                tradeEntrustDo.getMockType(),
                tradeEntrustDo.getCode()
        );
        if(tradePosition ==null){
            return OutputResult.buildAlert(ResultCode.TRADE_POSITION_NO);
        }
        tradePosition.setUseAmount(
                tradePosition.getUseAmount()
                        + tradeEntrustDo.getEntrustNum()
        );
        //更新
       tradePositionService.updateById(tradePosition);
        return OutputResult.buildSucc();
    }

    private OutputResult buyRevoke(TradeEntrustDo tradeEntrustDo) {
        //更新
        tradeEntrustDomainService.updateById(tradeEntrustDo);

        //将金额回滚
        TradeMoney tradeMoney =  tradeMoneyService.getByUserIdAndMockType(
                tradeEntrustDo.getUserId(),
                tradeEntrustDo.getMockType()
        );
        if(null== tradeMoney){
            return OutputResult.buildAlert(ResultCode.TRADE_NO_MONEY);
        }
        //获取到后，需要进行更新
        tradeMoney.setUseMoney(
                BigDecimalUtil.addBigDecimal(
                        tradeMoney.getUseMoney(),
                        tradeEntrustDo.getUseMoney()
                )
        );
        tradeMoney.setTakeoutMoney(
                BigDecimalUtil.addBigDecimal(
                        tradeMoney.getTakeoutMoney(),
                        tradeEntrustDo.getTakeoutMoney()
                )
        );
        tradeMoneyService.updateMoney(tradeMoney);
        return OutputResult.buildSucc();
    }
}
