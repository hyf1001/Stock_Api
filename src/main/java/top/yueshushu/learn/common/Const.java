package top.yueshushu.learn.common;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName:Const
 * @Description 定义一些常量
 * @Author 岳建立
 * @Date 2022/1/2 10:51
 * @Version 1.0
 **/
public class Const {
    public static final String SALT = "twoButterfly";
    public static final String RAND_NUMBER_PREFIX = "0.903";
    public static final Integer DEFAULT_NO = 0;
    public static final Integer DEFAULT_USER_ID = 1;
    public static final Integer DEFAULT_REBOOT_ID = 1;
    public static final String TRADE_PASSWORD_AES_KEY = "yuezelinyuezelin";

    public static final String CACHE_PUBLIC_KEY_PREFIX = "stock_public:";
    public static final String CACHE_PRIVATE_KEY_PREFIX = "stock_private:";
    public static final String CACHE_WE_CHAT = "stock:wechat";

    public static final String STOCK_PRICE = CACHE_PUBLIC_KEY_PREFIX + "now:";
    public static final String STOCK_YES_CLOSE_PRICE = CACHE_PUBLIC_KEY_PREFIX + "yes:close:";
    public static final String STOCK_YES_OPENING_PRICE = CACHE_PUBLIC_KEY_PREFIX + "yes:opening:";
    public static final String STOCK_YES_HIGH_PRICE = CACHE_PUBLIC_KEY_PREFIX + "yes:high:";
    public static final String STOCK_YES_LOWEST_PRICE = CACHE_PUBLIC_KEY_PREFIX + "yes:lowest:";
    public static final String STOCK_YES_ZT_CODE = CACHE_PUBLIC_KEY_PREFIX + "yes:zt";
    public static final String STOCK_TODAY_ZT_CODE = CACHE_PUBLIC_KEY_PREFIX + "today:zt";
    public static final String STOCK_TODAY_QS_CODE = CACHE_PUBLIC_KEY_PREFIX + "today:qs";


    public static final String STOCK_LAST_BUY_PRICE = CACHE_PRIVATE_KEY_PREFIX + "last_buy:";
    public static final String STOCK_LAST_SELL_PRICE = CACHE_PRIVATE_KEY_PREFIX + "last_sell:";

    public static final String STOCK_TODAY_BUY_PRICE = CACHE_PRIVATE_KEY_PREFIX + "today_buy:";
    public static final String STOCK_TODAY_SELL_PRICE = CACHE_PRIVATE_KEY_PREFIX + "today_sell:";

    public static final String STOCK_TODAY_BUY_NUM = CACHE_PRIVATE_KEY_PREFIX + "today_buy_num:";
    public static final String STOCK_TODAY_BUY_DB_NUM = CACHE_PRIVATE_KEY_PREFIX + "today_buy_db_num:";
    public static final String STOCK_TODAY_SELL_NUM = CACHE_PRIVATE_KEY_PREFIX + "today_sell_num:";

    public static final String STOCK_TODAY_DB_CODE = CACHE_PRIVATE_KEY_PREFIX + "today_db:";


    public static final String X_REAL_IP = "x-real-ip";
    public static final int TOKEN_EXPIRE_TIME = 7 * 24 * 3600;
    public static final int STOCK_PRICE_EXPIRE_TIME = 3 * 24 * 3600;
    public static final int STOCK_TODAY_PRICE_EXPIRE_TIME = 7 * 3600;
    public static final int JOB_CRON_EXPIRE_TIME = 7 * 24 * 3600;

    public static final int YZM_TIME = 60;
    //采用的是 md5 加密
    public static final String FOR_ALL_TIME_TOKEN = "b91f1d0a2849df8430e13bd12f80287a";

    /**
     * 定义日期的格式
     */
    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String STOCK_DATE_FORMAT = "yyyyMMdd";


    public static final String ASYNC_SERVICE_EXECUTOR_BEAN_NAME = "asyncServiceExecutor";

    public static final String SIMPLE_ASYNC_SERVICE_EXECUTOR_BEAN_NAME = "simpleAsyncServiceExecutor";

    public static final String HOLIDAY_CALENDAR_CACHE = "holiday_calendar";

    public static final String KEY_AUTH_USER_ID = "user_id";
    public static final String POSITION_HISTORY = "ph:";
    public static final String DATE_WORKING = "work:";
    public static final String JOB_INFO = "jobInfo:";
    public static final String RSA_PUBLIC_KEY = "publicKey";
    public static final String RSA_PRIVATE_KEY = "privateKey";

    public static final String CACHE_STOCK_INFO = Const.CACHE_PUBLIC_KEY_PREFIX + "info";


    public static final String Jasypt_KEY = "enc_safe";


    public static final List<String> IgnoreList = Arrays.asList("class", "userId", "method");

    public static final BigDecimal DEFAULT_PRICE = BigDecimal.ZERO;

    public static volatile Integer PRICE_COUNTER = 0;
}
