package top.yueshushu.learn.business.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.yueshushu.learn.business.TradeUserBusiness;
import top.yueshushu.learn.enumtype.MockType;
import top.yueshushu.learn.mode.ro.TradeUserRo;
import top.yueshushu.learn.mode.vo.MenuVo;
import top.yueshushu.learn.mode.vo.TradeUserVo;
import top.yueshushu.learn.response.OutputResult;
import top.yueshushu.learn.service.MenuService;
import top.yueshushu.learn.service.TradeUserService;
import top.yueshushu.learn.service.UserService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 交易用户实现编排层
 * @Author yuejianli
 * @Date 2022/5/20 23:54
 **/
@Service
@Slf4j

public class TradeUserBusinessImpl implements TradeUserBusiness {
    @Resource
    private TradeUserService tradeUserService;
    @Resource
    private MenuService menuService;
    @Resource
    private UserService userService;

    @Override
    public OutputResult login(TradeUserRo tradeUserRo) {

        OutputResult outputResult = tradeUserService.login(tradeUserRo);
        if (!outputResult.getSuccess()) {
            return outputResult;
        }
        TradeUserVo tradeUserVo = (TradeUserVo) outputResult.getData();

        //获取权限信息
        List<MenuVo> menuVoList = menuService.listMenuListByUid(tradeUserRo.getId());
        tradeUserVo.setMenuList(menuVoList);

        return OutputResult.buildSucc(tradeUserVo);
    }

    @Override
    public OutputResult editInfo(TradeUserRo tradeUserRo) {
        // 对 账号进行加密.
        tradeUserRo.setAccount(userService.tradeUserText(tradeUserRo.getAccount()).getData().toString());
        // 对密码进行处理。
        tradeUserRo.setPassword(userService.tradeUserText(tradeUserRo.getPassword()).getData().toString());

        // 更新.
        tradeUserService.editInfo(tradeUserRo);

        return OutputResult.buildSucc();
    }

    @Override
    public boolean configTradeUser(Integer userId, MockType mockType) {
        if (mockType == null) {
            return false;
        }
        if (MockType.MOCK.equals(mockType)) {
            return true;
        }
        return tradeUserService.configTradeUser(userId);
    }
}
