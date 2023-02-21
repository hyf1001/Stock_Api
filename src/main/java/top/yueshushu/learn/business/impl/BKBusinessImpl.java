package top.yueshushu.learn.business.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.yueshushu.learn.assembler.StockBkMoneyHistoryAssembler;
import top.yueshushu.learn.business.BKBusiness;
import top.yueshushu.learn.common.Const;
import top.yueshushu.learn.common.ResultCode;
import top.yueshushu.learn.crawler.crawler.ExtCrawlerService;
import top.yueshushu.learn.crawler.entity.BKInfo;
import top.yueshushu.learn.crawler.entity.BKMoneyInfo;
import top.yueshushu.learn.crawler.entity.StockBKStockInfo;
import top.yueshushu.learn.domain.StockBkDo;
import top.yueshushu.learn.domain.StockBkMoneyHistoryDo;
import top.yueshushu.learn.domain.StockBkStockDo;
import top.yueshushu.learn.domainservice.StockBkStockDomainService;
import top.yueshushu.learn.enumtype.BKCharMoneyType;
import top.yueshushu.learn.enumtype.BKType;
import top.yueshushu.learn.helper.DateHelper;
import top.yueshushu.learn.mode.ro.StockBKMoneyStatRo;
import top.yueshushu.learn.mode.vo.StockBKVo;
import top.yueshushu.learn.mode.vo.charinfo.LineSeriesVo;
import top.yueshushu.learn.mode.vo.charinfo.LineVo;
import top.yueshushu.learn.response.OutputResult;
import top.yueshushu.learn.service.StockBkMoneyHistoryService;
import top.yueshushu.learn.service.StockBkService;
import top.yueshushu.learn.util.BigDecimalUtil;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 版块的 ServiceImpl
 *
 * @author yuejianli
 * @date 2023-02-07
 */
@Service
@Slf4j
public class BKBusinessImpl implements BKBusiness {
    @Resource
    private StockBkService stockBkService;
    @Resource
    private StockBkMoneyHistoryService stockBkMoneyHistoryService;
    @Resource
    private StockBkMoneyHistoryAssembler stockBkMoneyHistoryAssembler;
    @Resource
    private ExtCrawlerService extCrawlerService;
    @Resource
    private DateHelper dateHelper;
    @Resource
    private StockBkStockDomainService stockBkStockDomainService;


    @Override
    public void syncBK() {
        handlerBK(BKType.BK);
    }

    @Override
    public void syncGN() {
        handlerBK(BKType.GN);
    }

    @Override
    public void syncDY() {
        handlerBK(BKType.DY);
    }

    @Override
    public void syncBKMoney() {
        handlerMoney(BKType.BK);
    }

    @Override
    public void syncGNMoney() {
        handlerMoney(BKType.GN);
    }

    @Override
    public void syncDYMoney() {
        handlerMoney(BKType.DY);
    }

    @Override
    public List<BKInfo> crawlerBKInfoByType(BKType bkType) {
        if (null == bkType) {
            return Collections.emptyList();
        }
        switch (bkType) {
            case BK: {
                return extCrawlerService.findAllBkList();
            }
            case GN: {
                return extCrawlerService.findAllGnList();
            }
            case DY: {
                return extCrawlerService.findAllDyList();
            }
            default: {
                break;
            }
        }
        return Collections.emptyList();
    }

    public void handlerBK(BKType bkType) {
        //1. 查询当前所有的版块
        List<BKInfo> allBkList = crawlerBKInfoByType(bkType);
        if (CollectionUtils.isEmpty(allBkList)) {
            sleep(3);
            allBkList = crawlerBKInfoByType(bkType);
            if (CollectionUtils.isEmpty(allBkList)) {
                return;
            }
        }
        Map<String, BKInfo> allBKCodeMap = allBkList.stream().collect(Collectors.toMap(BKInfo::getCode, n -> n));
        //2. 查询数据库中的版块
        List<StockBkDo> stockBkDoList = stockBkService.listByOrder(bkType);

        Map<String, StockBkDo> dbStockCodeMap = stockBkDoList.stream().collect(Collectors.toMap(StockBkDo::getCode, n -> n));
        //3. 进行比对， 找出 添加的，修改的 和删除的

        List<StockBkDo> addStockDoList = new ArrayList<>();
        List<StockBkDo> updateStockDoList = new ArrayList<>();
        List<Integer> deleteStockIdList = new ArrayList<>();
        //4. 分别进行处理。
        for (BKInfo bkInfo : allBkList) {
            if (!dbStockCodeMap.containsKey(bkInfo.getCode())) {
                StockBkDo stockBkDo = new StockBkDo();
                stockBkDo.setCode(bkInfo.getCode());
                stockBkDo.setName(bkInfo.getName());
                stockBkDo.setType(bkInfo.getType());
                addStockDoList.add(stockBkDo);
            } else {
                StockBkDo oldBkDO = dbStockCodeMap.get(bkInfo.getCode());
                if (bkInfo.getName().equals(oldBkDO.getName())) {
                    continue;
                }
                oldBkDO.setName(bkInfo.getName());
                updateStockDoList.add(oldBkDO);
            }
        }

        // 处理删除的
        dbStockCodeMap.entrySet().forEach(
                n -> {
                    StockBkDo stockBKDo = n.getValue();
                    if (!allBKCodeMap.containsKey(stockBKDo.getCode())) {
                        deleteStockIdList.add(stockBKDo.getId());
                    }
                }
        );
        //接下来，进行插入和修改相关操作.
        stockBkService.removeByIds(deleteStockIdList);
        stockBkService.updateBatchById(updateStockDoList);
        stockBkService.saveBatch(addStockDoList);
    }


    @Override
    public List<BKMoneyInfo> crawlerMoneyInfoByType(BKType bkType) {
        if (null == bkType) {
            return Collections.emptyList();
        }
        switch (bkType) {
            case BK: {
                return extCrawlerService.findTodayBkMoneyList();
            }
            case GN: {
                return extCrawlerService.findTodayGnMoneyList();
            }
            case DY: {
                return extCrawlerService.findTodayDyMoneyList();
            }
            default: {
                break;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public OutputResult<List<BKMoneyInfo>> getMoneyHistoryInfoByCode(StockBKMoneyStatRo stockBKMoneyStatRo) {
        // 根据版块编码 获取信息.
        String bkCode = stockBKMoneyStatRo.getBkCode();
        StockBkDo stockBkDo = stockBkService.selectByCode(bkCode);
        if (stockBkDo == null) {
            return OutputResult.buildAlert(ResultCode.BK_CODE_NOT_EXIST);
        }
        Date startDate = DateUtil.parse(stockBKMoneyStatRo.getStartDate(), DatePattern.NORM_DATE_PATTERN);
        Date endDate = DateUtil.endOfDay(DateUtil.date());
        int size = 0;
        //   long subDay = DateUtil.between(startDate, endDate, DateUnit.DAY);
        while (endDate.after(startDate)) {
            // startDate 进行加1
            if (dateHelper.isWorkingDay(startDate)) {
                size++;
            }
            startDate = DateUtil.offsetDay(startDate, 1);
        }
        String secid = "90." + bkCode;
        BKType bkType = BKType.getType(stockBkDo.getType());

        List<BKMoneyInfo> historyBkMoneyList = extCrawlerService.findHistoryBkMoneyList(size, secid, bkType);

        if (CollectionUtils.isEmpty(historyBkMoneyList)) {
            return OutputResult.buildAlert(ResultCode.ALERT);
        }
        // 如果同步的话，进行处理数据。
        if (stockBKMoneyStatRo.isAsync()) {
            List<BKMoneyInfo> asyncResultList = new ArrayList<>();
            // 看是否有数据， 没有的话，则添加.
            List<StockBkMoneyHistoryDo> existResultList = stockBkMoneyHistoryService.getMoneyHistoryByCodeAndRangeDate(bkCode, startDate, endDate);
            List<String> existDateList = existResultList.stream().map(n -> DateUtil.format(n.getCurrentDate(), DatePattern.NORM_DATE_PATTERN)).collect(Collectors.toList());
            for (BKMoneyInfo bkMoneyInfo : historyBkMoneyList) {
                if (!existDateList.contains(bkMoneyInfo.getCurrentDateStr())) {
                    asyncResultList.add(bkMoneyInfo);
                }
            }
            List<StockBkMoneyHistoryDo> stockBkMoneyHistoryDoList = stockBkMoneyHistoryAssembler.toDoList(asyncResultList);
            stockBkMoneyHistoryService.saveBatch(stockBkMoneyHistoryDoList, 100);
        }
        return OutputResult.buildSucc(historyBkMoneyList);
    }

    private void handlerMoney(BKType bkType) {
        List<BKMoneyInfo> todayMoneyInfoList = crawlerMoneyInfoByType(bkType);
        if (CollectionUtils.isEmpty(todayMoneyInfoList)) {
            sleep(3);
            todayMoneyInfoList = crawlerMoneyInfoByType(bkType);
            if (CollectionUtils.isEmpty(todayMoneyInfoList)) {
                return;
            }
        }

        // 删除当前日期的
        stockBkMoneyHistoryService.deleteByDate(DateUtil.date(), bkType);
        List<StockBkMoneyHistoryDo> stockBkMoneyHistoryDoList = stockBkMoneyHistoryAssembler.toDoList(todayMoneyInfoList);
        stockBkMoneyHistoryService.saveBatch(stockBkMoneyHistoryDoList, 100);
    }


    @Override
    public List<StockBKVo> listByType(BKType bkType) {
        if (null == bkType) {
            return Collections.emptyList();
        }

        List<StockBkDo> stockBkDoList = new ArrayList<>();
        switch (bkType) {
            case BK: {
                stockBkDoList = stockBkService.listByOrder(BKType.BK);
                break;
            }
            case GN: {
                stockBkDoList = stockBkService.listByOrder(BKType.GN);
                break;
            }
            case DY: {
                stockBkDoList = stockBkService.listByOrder(BKType.DY);
                break;
            }
            default: {
                break;
            }
        }
        return stockBkMoneyHistoryAssembler.bkVoList(stockBkDoList);
    }


    @Override
    public OutputResult<List<StockBKVo>> listBk() {
        return OutputResult.buildSucc(listByType(BKType.BK));
    }

    @Override
    public OutputResult<List<StockBKVo>> listGn() {
        return OutputResult.buildSucc(listByType(BKType.GN));
    }

    @Override
    public OutputResult<List<StockBKVo>> listDy() {
        return OutputResult.buildSucc(listByType(BKType.DY));
    }

    @Override
    public OutputResult getCharStat(StockBKMoneyStatRo stockBKMoneyStatRo) {
        StockBkDo stockBkDo = stockBkService.selectByCode(stockBKMoneyStatRo.getBkCode());
        if (stockBkDo == null) {
            return OutputResult.buildAlert(
                    ResultCode.BK_CODE_IS_EMPTY
            );
        }

        BKCharMoneyType[] values = BKCharMoneyType.values();
        List<String> legendList = new ArrayList<>();

        if (CollectionUtils.isEmpty(stockBKMoneyStatRo.getCharBKTypeList())) {
            for (BKCharMoneyType bkCharMoneyType : values) {
                legendList.add(bkCharMoneyType.getDesc());
            }
        } else {
            legendList.addAll(stockBKMoneyStatRo.getCharBKTypeList());
        }
        //获取范围
        List<String> xaxisData = new ArrayList<>();
        //获取开始日期和结束日期
        String startDate = stockBKMoneyStatRo.getStartDate();
        String endDate = stockBKMoneyStatRo.getEndDate();
        //计算开始日期和结束日期相差多少天，就是后面的计算值.
        DateTime startDateDate = DateUtil.parse(startDate, Const.SIMPLE_DATE_FORMAT);
        DateTime endDateDate = DateUtil.parse(endDate, Const.SIMPLE_DATE_FORMAT);
        //计算一下，相差多少天
        long day = DateUtil.betweenDay(startDateDate, endDateDate, true);
        //进行计算
        for (int i = 0; i <= day; i++) {
            DateTime tempDate = DateUtil.offsetDay(startDateDate, i);
            if (dateHelper.isWorkingDay(tempDate)) {
                xaxisData.add(DateUtil.format(tempDate, Const.SIMPLE_DATE_FORMAT));
            }
        }
        LineVo lineVo = new LineVo();
        lineVo.setXaxisData(xaxisData);
        lineVo.setLegend(legendList);
        //计算拼接信息.
        List<StockBkMoneyHistoryDo> stockBkMoneyDoList = stockBkMoneyHistoryService.getMoneyHistoryByCodeAndRangeDate(
                stockBKMoneyStatRo.getBkCode(),
                startDateDate,
                endDateDate
        );
        if (CollectionUtils.isEmpty(stockBkMoneyDoList)) {
            return OutputResult.buildSucc(lineVo);
        }
        //进行处理.
        List<LineSeriesVo> lineSeriesVoList = historyConvertLine(stockBkMoneyDoList, xaxisData.size());

        // 进行过滤，按照类型进行过滤。
        lineSeriesVoList = lineSeriesVoList.stream().filter(n -> legendList.contains(n.getName())).collect(Collectors.toList());
        lineVo.setSeries(lineSeriesVoList);


        return OutputResult.buildSucc(lineVo);
    }

    @Override
    public OutputResult<List<StockBKVo>> listMoneyType() {

        BKCharMoneyType[] values = BKCharMoneyType.values();

        List<StockBKVo> result = new ArrayList<>(values.length);

        for (BKCharMoneyType bkCharMoneyType : values) {

            StockBKVo stockBKVo = new StockBKVo();
            stockBKVo.setCode(bkCharMoneyType.getCode() + "");
            stockBKVo.setName(bkCharMoneyType.getDesc());

            result.add(stockBKVo);
        }

        return OutputResult.buildSucc(result);


    }

    @Override
    public void syncBkAndMoney() {
        syncBK();
        syncBKMoney();
        syncGN();
        syncGNMoney();
        syncDY();
        syncDYMoney();
    }

    @Override
    public void syncRelationCode(String code) {
        //1. 查询当前所有的版块
        List<StockBKStockInfo> allBkList = extCrawlerService.findRelationBkListByCode(code);
        if (CollectionUtils.isEmpty(allBkList)) {
            sleep(3);
            allBkList = extCrawlerService.findRelationBkListByCode(code);
            if (CollectionUtils.isEmpty(allBkList)) {
                return;
            }
        }
        Map<String, StockBKStockInfo> allBKCodeMap = allBkList.stream().collect(Collectors.toMap(StockBKStockInfo::getBkCode, n -> n));
        //2. 查询数据库中的版块
        List<StockBkStockDo> stockBkDoList = stockBkStockDomainService.listByStockCode(code, null);

        Map<String, StockBkStockDo> dbStockCodeMap = stockBkDoList.stream().collect(Collectors.toMap(StockBkStockDo::getBkCode, n -> n));
        //3. 进行比对， 找出 添加的，修改的 和删除的

        List<StockBkStockDo> addStockDoList = new ArrayList<>();
        List<Integer> deleteStockIdList = new ArrayList<>();
        //4. 分别进行处理。
        for (StockBKStockInfo stockBKStockInfo : allBkList) {
            if (!dbStockCodeMap.containsKey(stockBKStockInfo.getBkCode())) {
                StockBkStockDo stockBkDo = new StockBkStockDo();
                stockBkDo.setStockCode(stockBKStockInfo.getStockCode());
                stockBkDo.setBkCode(stockBKStockInfo.getBkCode());
                addStockDoList.add(stockBkDo);
            }
        }

        // 处理删除的
        dbStockCodeMap.entrySet().forEach(
                n -> {
                    StockBkStockDo stockBKDo = n.getValue();
                    if (!allBKCodeMap.containsKey(stockBKDo.getBkCode())) {
                        deleteStockIdList.add(stockBKDo.getId());
                    }
                }
        );
        //接下来，进行插入和修改相关操作.
        stockBkStockDomainService.removeByIds(deleteStockIdList);
        stockBkStockDomainService.saveBatch(addStockDoList);


    }
    /**
     * 将历史数据转换成 图表数据
     *
     * @param stockBkMoneyDoList 历史数据
     * @return 将历史数据转换成 图表数据
     */
    private List<LineSeriesVo> historyConvertLine(List<StockBkMoneyHistoryDo> stockBkMoneyDoList, int size) {
        List<LineSeriesVo> result = new ArrayList<>();

        // 新值
        LineSeriesVo bkNowPrice = new LineSeriesVo();
        bkNowPrice.setName(BKCharMoneyType.BK_NOW_PRICE.getDesc());

        LineSeriesVo bkNowProportion = new LineSeriesVo();
        bkNowProportion.setName(BKCharMoneyType.BK_NOW_PROPORTION.getDesc());

        // 主力
        LineSeriesVo todayMainInflow = new LineSeriesVo();
        todayMainInflow.setName(BKCharMoneyType.TODAY_MAIN_INFLOW.getDesc());

        LineSeriesVo todayMainInflowProportion = new LineSeriesVo();
        todayMainInflowProportion.setName(BKCharMoneyType.TODAY_MAIN_INFLOW_PROPORTION.getDesc());

        // 超级大

        LineSeriesVo todaySuperInflow = new LineSeriesVo();
        todaySuperInflow.setName(BKCharMoneyType.TODAY_SUPER_INFLOW.getDesc());

        LineSeriesVo todaySuperInflowProportion = new LineSeriesVo();
        todaySuperInflowProportion.setName(BKCharMoneyType.TODAY_SUPER_INFLOW_PROPORTION.getDesc());


        // 大

        LineSeriesVo todayMoreInflow = new LineSeriesVo();
        todayMoreInflow.setName(BKCharMoneyType.TODAY_MORE_INFLOW.getDesc());

        LineSeriesVo todayMoreInflowProportion = new LineSeriesVo();
        todayMoreInflowProportion.setName(BKCharMoneyType.TODAY_MORE_INFLOW_PROPORTION.getDesc());


        // 中

        LineSeriesVo todayMiddleInflow = new LineSeriesVo();
        todayMiddleInflow.setName(BKCharMoneyType.TODAY_MIDDLE_INFLOW.getDesc());

        LineSeriesVo todayMiddleInflowProportion = new LineSeriesVo();
        todayMiddleInflowProportion.setName(BKCharMoneyType.TODAY_MIDDLE_INFLOW_PROPORTION.getDesc());


        // 小

        LineSeriesVo todaySmallInflow = new LineSeriesVo();
        todaySmallInflow.setName(BKCharMoneyType.TODAY_SMALL_INFLOW.getDesc());

        LineSeriesVo todaySmallInflowProportion = new LineSeriesVo();
        todaySmallInflowProportion.setName(BKCharMoneyType.TODAY_SMALL_INFLOW_PROPORTION.getDesc());



        /*
         bkNowPrice  bkNowProportion  todayMainInflow todayMainInflowProportion
         todaySuperInflow   todaySuperInflowProportion  todayMoreInflow  todayMoreInflowProportion
         todayMiddleInflow  todayMiddleInflowProportion  todaySmallInflow todaySmallInflowProportion
         */

        //处理信息
        for (StockBkMoneyHistoryDo stockBkMoneyHistoryDo : stockBkMoneyDoList) {
            bkNowPrice.getData().add(BigDecimalUtil.toDouble(stockBkMoneyHistoryDo.getBkNowPrice()));
            bkNowProportion.getData().add(BigDecimalUtil.toDouble(new BigDecimal(stockBkMoneyHistoryDo.getBkNowProportion())));
            todayMainInflow.getData().add(BigDecimalUtil.toDouble(new BigDecimal(stockBkMoneyHistoryDo.getTodayMainInflow())));
            todayMainInflowProportion.getData().add(BigDecimalUtil.toDouble(new BigDecimal(stockBkMoneyHistoryDo.getTodayMainInflowProportion())));
            todaySuperInflow.getData().add(BigDecimalUtil.toDouble(new BigDecimal(stockBkMoneyHistoryDo.getTodaySuperInflow())));
            todaySuperInflowProportion.getData().add(BigDecimalUtil.toDouble(new BigDecimal(stockBkMoneyHistoryDo.getTodaySuperInflowProportion())));
            todayMoreInflow.getData().add(BigDecimalUtil.toDouble(new BigDecimal(stockBkMoneyHistoryDo.getTodayMoreInflow())));
            todayMoreInflowProportion.getData().add(BigDecimalUtil.toDouble(new BigDecimal(stockBkMoneyHistoryDo.getTodayMoreInflowProportion())));
            todayMiddleInflow.getData().add(BigDecimalUtil.toDouble(new BigDecimal(stockBkMoneyHistoryDo.getTodayMiddleInflow())));
            todayMiddleInflowProportion.getData().add(BigDecimalUtil.toDouble(new BigDecimal(stockBkMoneyHistoryDo.getTodayMiddleInflowProportion())));
            todaySmallInflow.getData().add(BigDecimalUtil.toDouble(new BigDecimal(stockBkMoneyHistoryDo.getTodaySmallInflow())));
            todaySmallInflowProportion.getData().add(BigDecimalUtil.toDouble(new BigDecimal(stockBkMoneyHistoryDo.getTodaySmallInflowProportion())));
        }

        // 长度不够的话，补 0

        if (bkNowPrice.getData().size() < size) {
            // 进行补 0 的操作.
            int subSize = size - bkNowPrice.getData().size();

            List<Double> subSizeList = new ArrayList<>(subSize);

            for (int i = 0; i < subSize; i++) {
                subSizeList.add(BigDecimal.ZERO.doubleValue());
            }

            bkNowPrice.getData().addAll(0, subSizeList);
            bkNowProportion.getData().addAll(0, subSizeList);
            todayMainInflow.getData().addAll(0, subSizeList);
            todayMainInflowProportion.getData().addAll(0, subSizeList);
            todaySuperInflow.getData().addAll(0, subSizeList);
            todaySuperInflowProportion.getData().addAll(0, subSizeList);
            todayMoreInflow.getData().addAll(0, subSizeList);
            todayMoreInflowProportion.getData().addAll(0, subSizeList);
            todayMiddleInflow.getData().addAll(0, subSizeList);
            todayMiddleInflowProportion.getData().addAll(0, subSizeList);
            todaySmallInflow.getData().addAll(0, subSizeList);
            todaySmallInflowProportion.getData().addAll(0, subSizeList);
        }


        result.add(bkNowPrice);
        result.add(bkNowProportion);
        result.add(todayMainInflow);
        result.add(todayMainInflowProportion);
        result.add(todaySuperInflow);
        result.add(todaySuperInflowProportion);

        result.add(todayMoreInflow);
        result.add(todayMoreInflowProportion);
        result.add(todayMiddleInflow);
        result.add(todayMiddleInflowProportion);
        result.add(todaySmallInflow);
        result.add(todaySmallInflowProportion);

        return result;
    }


    private void sleep(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (Exception e) {

        }
    }
}
