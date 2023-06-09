package top.yueshushu.learn.enumtype;

import org.springframework.util.Assert;

/**
 * 交易定时任务
 *
 * @author 两个蝴蝶飞
 */
public enum JobInfoType {
    HOLIDAY("holiday", "假期同步"),
    POSITION_USE_AMOUNT("positionUseAmount", "同步可用数量"),
    YESTERDAY_PRICE("yesterdayPrice", "同步上一交易日价格"),
    STOCK_HISTORY("stockHistory", "股票这一交易日历史同步"),
    STOCK_PRICE("stockPrice", "实时获取股票价格"),
    TRADE_ING_TO_REVOKE("tradeIngToRevoke", "自动撤消委托"),
    TRADE_POSITION_HISTORY("tradePositionHistory", "交易持仓信息同步"),
    CALL_PROFIT("callProfit", "计算每天的盈亏数目"),
    MOCK_DEAL("mockDeal", "虚拟成交"),
    MOCK_ENTRUST("mockEntrust", "虚拟委托"),
    STOCK_UPDATE("stockUpdate", "股票更新"),
    STOCK_FIVE_EMAIL("stockFiveEmail", "最近五天记录发送到邮箱"),
    BIG_DEAL("bigDeal", "大宗交易信息同步"),
    SYNC_EASY_MONEY("syncEasyMoney", "同步东财数据保存到数据库"),
    BUY_NEW_STOCK("buyNewStock", "新股申购"),
    AUTO_LOGIN("autoLogin", "自动登录"),
    STOCK_BK("stockBk", "股票版块"),
    STOCK_PRICE_SAVE("stockPriceSave", "股票价格每分钟保存"),
    DB_STOCK_TRADE("dbStockTrade", "股票打版买入"),
    STOCK_POOL_QS("stockPoolQs", "股票池强势股"),
    STOCK_POOL("stockPool", "股票池分析"),
    PRICE_IMAGE("priceImage", "价格分时图保存")
    ;

    private String code;

    private String desc;

    private JobInfoType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取定时任务信息
     *
     * @param code
     * @return
     */
    public static JobInfoType getJobInfoType(String code) {
        Assert.notNull(code, "code编号不能为空");
        for (JobInfoType exchangeType : JobInfoType.values()) {
            if (exchangeType.code.equalsIgnoreCase(code)) {
                return exchangeType;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
