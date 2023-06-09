package top.yueshushu.learn.business.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.yueshushu.learn.business.TradeRuleConditionBusiness;
import top.yueshushu.learn.mode.ro.TradeRuleConditionRo;
import top.yueshushu.learn.mode.vo.TradeRuleConditionVo;
import top.yueshushu.learn.response.OutputResult;
import top.yueshushu.learn.service.TradeRuleConditionService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 交易规则条件的信息实现
 * @Author 岳建立
 * @Date 2022/5/20 22:33
 **/
@Service
@Slf4j
public class TradeRuleConditionBusinessImpl implements TradeRuleConditionBusiness {
    @Resource
    private TradeRuleConditionService tradeRuleConditionService;

    @Override
    public OutputResult<List<TradeRuleConditionVo>> list() {
        return tradeRuleConditionService.listCondition();
    }

    @Override
    public OutputResult updateCondition(TradeRuleConditionRo tradeRuleConditionRo) {
        return tradeRuleConditionService.updateCondition(tradeRuleConditionRo);
    }

    @Override
    public OutputResult addCondition(TradeRuleConditionRo tradeRuleConditionRo) {
        return tradeRuleConditionService.addCondition(tradeRuleConditionRo);
    }

    @Override
    public OutputResult deleteCondition(Integer id) {
        return tradeRuleConditionService.deleteCondition(id);
    }
}
