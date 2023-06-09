package top.yueshushu.learn.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.yueshushu.learn.assembler.StockSelectedAssembler;
import top.yueshushu.learn.common.Const;
import top.yueshushu.learn.common.ResultCode;
import top.yueshushu.learn.crawler.crawler.ExtCrawlerService;
import top.yueshushu.learn.crawler.entity.StockIndexInfo;
import top.yueshushu.learn.crawler.service.CrawlerStockHistoryService;
import top.yueshushu.learn.domain.StockPriceHistoryDo;
import top.yueshushu.learn.domain.StockSelectedDo;
import top.yueshushu.learn.domainservice.StockDomainService;
import top.yueshushu.learn.domainservice.StockPriceHistoryDomainService;
import top.yueshushu.learn.domainservice.StockSelectedDomainService;
import top.yueshushu.learn.entity.Stock;
import top.yueshushu.learn.entity.StockHistory;
import top.yueshushu.learn.entity.StockSelected;
import top.yueshushu.learn.enumtype.DBStockType;
import top.yueshushu.learn.enumtype.DataFlagType;
import top.yueshushu.learn.enumtype.SyncStockHistoryType;
import top.yueshushu.learn.mode.dto.StockPriceCacheDto;
import top.yueshushu.learn.mode.ro.IdRo;
import top.yueshushu.learn.mode.ro.StockRo;
import top.yueshushu.learn.mode.ro.StockSelectedRo;
import top.yueshushu.learn.mode.vo.StockSelectedVo;
import top.yueshushu.learn.response.OutputResult;
import top.yueshushu.learn.response.PageResponse;
import top.yueshushu.learn.service.StockCrawlerService;
import top.yueshushu.learn.service.StockHistoryService;
import top.yueshushu.learn.service.StockSelectedService;
import top.yueshushu.learn.service.StockService;
import top.yueshushu.learn.service.cache.StockCacheService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 股票自选表,是用户自己选择的 自定义的
 * </p>
 *
 * @author 岳建立
 * @since 2022-01-02
 */
@Service
@Slf4j(topic = "自选股票")
public class StockSelectedServiceImpl implements StockSelectedService {

    @Resource
    private StockSelectedAssembler stockSelectedAssembler;
    @Resource
    private StockSelectedDomainService stockSelectedDomainService;
    @Resource
    private StockCacheService stockCacheService;

    @Resource
    private StockService stockService;


    @Resource
    private StockHistoryService stockHistoryService;

    @Resource
    private StockCrawlerService stockCrawlerService;
    @Resource
    private CrawlerStockHistoryService crawlerStockHistoryService;
    @Resource
    private StockPriceHistoryDomainService stockPriceHistoryDomainService;
    @Resource
    private StockDomainService stockDomainService;
    @Resource
    private ExtCrawlerService extCrawlerService;

    @SuppressWarnings("all")
    @Resource(name = Const.ASYNC_SERVICE_EXECUTOR_BEAN_NAME)
    private AsyncTaskExecutor executor;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public StockSelected add(StockSelectedRo stockSelectedRo, String stockName) {
        StockSelected stockSelected = stockSelectedAssembler.doToEntity(
                stockSelectedDomainService.getByUserIdAndCodeAndStatus(
                        stockSelectedRo.getUserId(),
                        stockSelectedRo.getStockCode(),
                        null
                )
        );
        if (stockSelected == null) {
            log.info("用户{}没有该股票 {} 的记录，进行添加", stockSelectedRo.getUserId(),stockSelectedRo.getStockCode());
            stockSelected = new StockSelected();
            stockSelected.setStockCode(stockSelectedRo.getStockCode());
            stockSelected.setStockName(
                    stockName
            );
            stockSelected.setNotes(stockSelectedRo.getNotes());
            stockSelected.setUserId(stockSelectedRo.getUserId());
            stockSelected.setCreateTime(DateUtil.date());
            stockSelected.setStatus(DataFlagType.NORMAL.getCode());
            stockSelected.setFlag(DataFlagType.NORMAL.getCode());

            stockSelectedDomainService.save(
                    stockSelectedAssembler.entityToDo(
                            stockSelected
                    )
            );
        } else {
            log.info("用户{} 有股票 {}自选记录,只能现在被删除了,对这条记录进行修改", stockSelectedRo.getUserId(),
                    stockSelectedRo.getStockCode());
            stockSelected.setCreateTime(DateUtil.date());
            stockSelected.setStatus(DataFlagType.NORMAL.getCode());
            stockSelected.setNotes(stockSelectedRo.getNotes());
            //进行更新
            stockSelectedDomainService.updateById(
                    stockSelectedAssembler.entityToDo(
                            stockSelected
                    )
            );
        }
        // 重新查询一下,获取最新的携带着 id的数据
       return stockSelectedAssembler.doToEntity(
                stockSelectedDomainService.getByUserIdAndCodeAndStatus(
                        stockSelectedRo.getUserId(),
                        stockSelectedRo.getStockCode(),
                        DataFlagType.NORMAL.getCode()
                )
        );
    }
    /**
     * 看是否超过最大的数量，如果超过，则返回 true
     * 如果没有超过，返回 false, 允许添加。
     * @param userId 用户编号
     * @param maxSelectedNum 最大的自选数量
     * @return 看是否超过最大的数量，如果超过，则返回 true
     */
    private boolean checkMaxCount(Integer userId,Integer maxSelectedNum) {
        //获取当前用户所拥有的数量.
        QueryWrapper<StockSelectedDo> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("flag", DataFlagType.NORMAL.getCode());
        int nowUseCount = stockSelectedDomainService.countByUserIdAndStatus(
                userId,
                DataFlagType.NORMAL.getCode()
        );
        //进行比较，返回
        return maxSelectedNum >= nowUseCount ? false : true;
    }

    @Override
    public OutputResult delete(IdRo idRo, int userId) {
        StockSelected stockSelected = stockSelectedAssembler.doToEntity(
                stockSelectedDomainService.getById(idRo.getId())
        );
        return deleteByInfo(stockSelected,userId);
    }

    public OutputResult deleteByInfo(StockSelected stockSelected,Integer userId){
        //如果不存在，或者用户编号不一致
        if (stockSelected == null || !stockSelected.getUserId().equals(userId)){
            return OutputResult.buildAlert(ResultCode.STOCK_SELECTED_NO_RECORD);
        }
        if (DataFlagType.DELETE.getCode().equals(stockSelected.getStatus())){
            return OutputResult.buildAlert(ResultCode.STOCK_SELECTED_HAVE_DISABLE);
        }

        //否则的话，修改成删除状态
        stockSelected.setStatus(
                DataFlagType.DELETE.getCode()
        );
        stockSelectedDomainService.updateById(
                stockSelectedAssembler.entityToDo(
                        stockSelected
                )
        );
        return OutputResult.buildSucc(stockSelected.getJobId());
    }

    @Override
    public OutputResult<PageResponse<StockSelectedVo>> pageSelected(StockSelectedRo stockSelectedRo) {
        Page<Object> pageGithubResult = PageHelper.startPage(stockSelectedRo.getPageNum(), stockSelectedRo.getPageSize());
        List<StockSelectedVo> pageResultList = listSelf(
                stockSelectedRo.getUserId(),
                stockSelectedRo.getKeyword()
        );
        if (CollectionUtils.isEmpty(pageResultList)) {
            return OutputResult.buildSucc(
                    PageResponse.emptyPageResponse()
            );
        }
        PageInfo pageInfo = new PageInfo<>(pageResultList);
        return OutputResult.buildSucc(new PageResponse<>(
                pageGithubResult.getTotal(), pageInfo.getList()
        ));
    }
    @Override
    public OutputResult validateAdd(StockSelectedRo stockSelectedRo, int maxSelectedNum) {
        StockSelected stockSelected = stockSelectedAssembler.doToEntity(
                stockSelectedDomainService.getByUserIdAndCodeAndStatus(
                        stockSelectedRo.getUserId(),
                        stockSelectedRo.getStockCode(),
                        DataFlagType.NORMAL.getCode()
                )
        );
        if (stockSelected != null &&
                DataFlagType.NORMAL.getCode().equals(
                        stockSelected.getStatus()
                )) {
            log.info("用户{}添加股票 {} 到自选失败，因为已经存在了",stockSelectedRo.getUserId(),
                    stockSelectedRo.getStockCode());
            return OutputResult.buildAlert(ResultCode.STOCK_SELECTED_EXISTS);
        }
        //看是否超过最大的数量
        if (checkMaxCount(stockSelectedRo.getUserId(),maxSelectedNum)) {
            return OutputResult.buildAlert(ResultCode.STOCK_SELECTED_MAX_LIMIT);
        }
        return OutputResult.buildSucc();
    }

    @Override
    public void updateSelected(StockSelected stockSelected) {
         stockSelectedDomainService.updateById(
                stockSelectedAssembler.entityToDo(
                        stockSelected
                )
        );
    }

    @Override
    public Map<String, String> listStockCodeByUserId(Integer userId) {

        List<StockSelectedDo> stockSelectedDoList = stockSelectedDomainService.listByUserIdAndCodeAndStatus(
                userId, null, DataFlagType.NORMAL.getCode()
        );
        return stockSelectedDoList.stream().collect(
                Collectors.toMap(
                        StockSelectedDo::getStockCode,
                        StockSelectedDo::getStockName,
                        (o,n)->n
                )
        );
    }

    @Override
    public void updateSelectedCodePrice(String code) {
//        if (StringUtils.hasText(code)){
//            executor.submit(
//                    ()-> {
//                        try {
//                            stockCrawlerService.updateCodePrice(code);
//                            TimeUnit.MILLISECONDS.sleep(50);
//                        } catch (Exception e) {
//
//                        }
//                    }
//            );
//
//            return ;
//        }
//        //查询出所有的自选表里面的股票编码
//        List<String> codeList = stockSelectedDomainService.findCodeList(null);
//        for (String selectedCode : codeList){
//            executor.submit(
//                    ()-> {
//                        try {
//                            stockCrawlerService.updateCodePrice(selectedCode);
//                            TimeUnit.MILLISECONDS.sleep(50);
//                        } catch (Exception e) {
//
//                        }
//                    }
//            );
//        }
        // 对股票编码进行处理。
        List<String> executeCodeList = new ArrayList<>();
        if (StringUtils.hasText(code)) {
            executeCodeList.add(code);
            List<String> finalExecuteCodeList1 = executeCodeList;
            executor.submit(
                    () -> {
                        try {
                            stockCrawlerService.batchUpdateNowPrice(finalExecuteCodeList1);
                            TimeUnit.MILLISECONDS.sleep(50);
                        } catch (Exception e) {

                        }
                    }
            );
            return;
        } else {
            List<String> codeList = stockSelectedDomainService.findCodeList(null);
            // 对股票编码进行分组， 每 5个为一组，进行查询。
            if (CollectionUtils.isEmpty(codeList)) {
                return;
            }
            int count = 5;
            for (int i = 0; i < codeList.size(); i = i + count) {
                int maxIndex = Math.min(i + count, codeList.size());
                executeCodeList = codeList.subList(i, maxIndex);
                List<String> finalExecuteCodeList = executeCodeList;
                executor.submit(
                        () -> {
                            try {
                                stockCrawlerService.batchUpdateNowPrice(finalExecuteCodeList);
                                TimeUnit.MILLISECONDS.sleep(50);
                            } catch (Exception e) {

                            }
                        }
                );
            }
        }
    }

    @Override
    public void updateStockIndexPrice(Integer type) {
        // 1. 查询出所有的信息.
        List<StockIndexInfo> stockIndexList = extCrawlerService.findStockIndex();
        // 如果不为空时，则进行筛选。
        if (type != null) {
            stockIndexList = stockIndexList.stream().filter(n -> type.equals(n.getType())).collect(Collectors.toList());
        }
        // 进行保存处理。 先删除之前的， 再进行重新设置。
        if (CollectionUtils.isEmpty(stockIndexList)) {
            return;
        }
        for (StockIndexInfo stockIndexInfo : stockIndexList) {
            stockCacheService.updateStockIndex(stockIndexInfo);
        }
    }

    @Override
    public OutputResult editNotes(StockSelectedRo stockSelectedRo) {
        StockSelected stockSelected = stockSelectedAssembler.doToEntity(
                stockSelectedDomainService.getById(
                        stockSelectedRo.getId()
                )
        );
        if (stockSelected == null) {
            return OutputResult.buildAlert(
                    ResultCode.STOCK_SELECTED_NO_RECORD
            );
        }
        stockSelected.setNotes(stockSelectedRo.getNotes());
        stockSelectedDomainService.updateById(
                stockSelectedAssembler.entityToDo(stockSelected)
        );
        return OutputResult.buildSucc();
    }

    @Override
    public List<String> batchSelected(int addUserId, List<String> stockCodeList) {
        // 查询一下 当前用户目前拥有的自选股票编码集合

        List<String> haveSelectedCodeList = stockSelectedDomainService.findCodeList(addUserId);
        // 进行移除
        stockCodeList.removeAll(haveSelectedCodeList);
        if (CollectionUtils.isEmpty(stockCodeList)) {
            log.info(">>>> 用户 {} 已经自选了所有的股票 {}", addUserId, haveSelectedCodeList);
        }
        log.info(">>>> 用户 {} 未自选了股票集合 {}", addUserId, stockCodeList);
        // 进行批量添加股票信息

        List<String> resultCodeList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(haveSelectedCodeList)) {
            resultCodeList.addAll(haveSelectedCodeList);
        }

        List<String> haveExistCodeList = findCodeList(null);

        for (String code : stockCodeList) {
            // 根据股票的编码，获取相应的股票记录信息
            Stock stock = stockCacheService.selectByCode(code);
            if (null == stock) {
                continue;
            }
            StockSelectedRo stockSelectedRo = new StockSelectedRo();
            stockSelectedRo.setStockCode(code);
            stockSelectedRo.setUserId(addUserId);

            add(stockSelectedRo, stock.getName());

            resultCodeList.add(code);
            executor.submit(() -> {
                if (!haveExistCodeList.contains(code)) {
                    syncCodeInfo(code);
                }
            });
        }
        return resultCodeList;
    }

    /**
     * 添加股票到自选后，同步股票的信息记录
     *
     * @param stockCode 股票编码
     */
    @Override
    public void syncCodeInfo(String stockCode) {
        // 同步股票的历史交易记录。 同步最近一个月的.
        StockRo stockRo = new StockRo();
        stockRo.setCode(stockCode);
        stockRo.setType(SyncStockHistoryType.MONTH.getCode());
        stockCrawlerService.stockHistoryAsync(stockRo);
        // 设置当前的价格信息
        stockCrawlerService.updateCodePrice(stockCode);


        //如果不存在的话
        StockHistory lastHistory = stockHistoryService.getLastHistory(stockCode);
        if (lastHistory == null) {
            return;
        }
        stockCacheService.setYesterdayCloseCachePrice(stockCode, lastHistory.getClosingPrice());
        stockCacheService.setYesterdayOpenCachePrice(stockCode, lastHistory.getOpeningPrice());
        stockCacheService.setYesterdayHighCachePrice(stockCode, lastHistory.getHighestPrice());
        stockCacheService.setYesterdayLowestCachePrice(stockCode, lastHistory.getLowestPrice());

    }

    @Override
    public List<String> findCodeList(Integer userId) {
        return stockSelectedDomainService.findCodeList(userId);
    }

    @Override
    public void saveStockPrice(String[] stockCodeList) {
        if (ArrayUtil.isEmpty(stockCodeList)) {
            return;
        }

        List<StockPriceHistoryDo> resultList = new ArrayList<>(stockCodeList.length);
        Date now = DateUtil.date();
        for (String stockCode : stockCodeList) {

            BigDecimal nowCachePrice = stockCacheService.getNowCachePrice(stockCode);
            if (nowCachePrice == null || BigDecimal.ZERO.equals(nowCachePrice)) {
                continue;
            }
            StockPriceHistoryDo stockPriceHistoryDo = new StockPriceHistoryDo();
            stockPriceHistoryDo.setCode(stockCode);
            stockPriceHistoryDo.setCurrTime(now);
            stockPriceHistoryDo.setPrice(nowCachePrice);
            resultList.add(stockPriceHistoryDo);
        }
        // 批量保存， 只保存，不删除。
        stockPriceHistoryDomainService.saveBatch(resultList);
    }

    @Override
    public OutputResult deleteByCode(StockSelectedRo stockSelectedRo) {
        StockSelected stockSelected = stockSelectedAssembler.doToEntity(
                stockSelectedDomainService.getByUserIdAndCodeAndStatus(
                        stockSelectedRo.getUserId(),
                        stockSelectedRo.getStockCode(),
                        null
                )
        );
       return deleteByInfo(stockSelected,stockSelected.getUserId());
    }

    @Override
    public void syncDayHistory() {
        //查询出所有的自选表里面的股票记录信息
        //List<String> codeSelectedList = stockSelectedDomainService.findCodeList(null);
        //for (String code : codeSelectedList) {
        //    //对股票进行同步
        //    StockRo stockRo = new StockRo();
        //    stockRo.setType(
        //            SyncStockHistoryType.SELF.getCode()
        //    );
        //    Date now = DateUtil.date();
        //    //获取当天的记录
        //    Date beforeDay = DateUtil.beginOfDay(now);
        //    stockRo.setStartDate(
        //            DateUtil.format(beforeDay, "yyyy-MM-dd hh:mm:ss")
        //    );
        //    stockRo.setEndDate(
        //            DateUtil.format(now, "yyyy-MM-dd hh:mm:ss")
        //    );
        //    stockRo.setCode(code);
        //    // 传入 1 ,1 表示股票
        //    stockRo.setExchange(1);
        //    stockCrawlerService.stockHistoryAsync(
        //            stockRo
        //    );
        //}
        List<String> codeSelectedList = syncSelectedCodeHistory(stockSelectedDomainService.findCodeList(null));
        syncShAndSZCodeHistory(codeSelectedList, DBStockType.SH_SZ_CY);
        // 同步指数信息.
        syncPointHistory(Arrays.asList("1.000001", "0.399001", "0.399006", "1.000300"));

    }

    /**
     * 同步指数列表信息
     *
     * @param pointCodeList 指数列表
     */
    public void syncPointHistory(List<String> pointCodeList) {
        crawlerStockHistoryService.syncPointHistory(pointCodeList);
    }

    private List<String> syncSelectedCodeHistory(List<String> codeList) {
        List<String> resultCodeList = codeList;
        // 15点之前
        executor.submit(
                () -> {
                    crawlerStockHistoryService.txMoneyTodayStockHistory(resultCodeList, stockService.listFullCode(resultCodeList));
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (Exception e) {
                    }
                }
        );
        return resultCodeList;
    }

    private void syncShAndSZCodeHistory(List<String> codeSelectedList, DBStockType dbStockType) {
        //1. 查询出所有的股票和对应的股票 full code 列表
        List<String> allCodeList = stockDomainService.listCodeByType(dbStockType);
        // 对股票 编码进行筛选
        List<String> filterCodeList = new ArrayList<>();
        for (String allCode : allCodeList) {
            if (codeSelectedList.contains(allCode)) {
                continue;
            }
            filterCodeList.add(allCode);
        }
        if (CollectionUtils.isEmpty(filterCodeList)) {
            return;
        }
        // 对股票进行分组处理.
        int size = filterCodeList.size();
        TimeInterval timeInterval = DateUtil.timer();
        log.info("开始同步全量的股票价格数据，共需要同步{}条", size);
        //进行分批保存, 最多是 20*99,不到2K条
        int BATCH_NUMBER = 20;
        for (int i = 0; i < size; i = i + BATCH_NUMBER) {
            int maxIndex = Math.min(i + BATCH_NUMBER, size);
            syncSelectedCodeHistory(filterCodeList.subList(i, maxIndex));
        }
        log.info("总同步时间是:" + timeInterval.interval());
    }

    @Override
    public void cacheClosePrice() {
        ////查询出所有的自选表里面的股票记录信息
        stockCacheService.clearYesPrice();

        List<String> codeList = stockSelectedDomainService.findCodeList(null);
        if (CollectionUtil.isEmpty(codeList)) {
            return;
        }
        //获取相关的信息，根据历史记录查询。
        List<StockPriceCacheDto> priceCacheDtoList = stockHistoryService.listClosePrice(codeList);
        // 获取到没有历史记录的股票信息.
        //筛选了所有的股票信息
        List<String> hasHistoryList = priceCacheDtoList.stream().map(StockPriceCacheDto::getCode).collect(Collectors.toList());
       List<String> noHistoryList = new ArrayList<>();
        if(CollectionUtil.isEmpty(hasHistoryList)){
            noHistoryList = codeList;
        }else{
            Collection<String> disjunction = CollectionUtil.disjunction(codeList, hasHistoryList);
            if (CollectionUtil.isNotEmpty(disjunction)){
                noHistoryList = new ArrayList<>(disjunction);
            }
        }
        //没有历史的数据。
        if (!CollectionUtils.isEmpty(noHistoryList)) {
            //东方财富同步没有的历史记录。
            // 换成成腾讯相应的接口信息
            // crawlerStockHistoryService.easyMoneyYesStockHistory(noHistoryList);
            // 早上执行的，同步的是昨天的。
            crawlerStockHistoryService.txMoneyYesStockHistory(noHistoryList, stockService.listFullCode(noHistoryList));
        }
        // 对每一条股票记录进行处理
        for (String code : codeList) {
            //如果不存在的话
            StockHistory lastHistory = stockHistoryService.getLastHistory(code);
            if (lastHistory == null) {
                continue;
            }
            stockCacheService.setYesterdayCloseCachePrice(code, lastHistory.getClosingPrice());
            stockCacheService.setYesterdayOpenCachePrice(code, lastHistory.getOpeningPrice());
            stockCacheService.setYesterdayHighCachePrice(code, lastHistory.getHighestPrice());
            stockCacheService.setYesterdayLowestCachePrice(code, lastHistory.getLowestPrice());
        }
    }

    @Override
    public List<StockSelectedVo> listSelf(Integer userId, String keyword) {
        //查询所有的股票信息
        List<StockSelectedDo> stockSelectedDoList =
                stockSelectedDomainService.listByUserIdAndKeyword(userId, keyword);
        if (CollectionUtils.isEmpty(stockSelectedDoList)) {
            return Collections.EMPTY_LIST;
        }
        List<StockSelectedVo> stockSelectedVoList = new ArrayList<>(stockSelectedDoList.size());
        stockSelectedDoList.forEach(
                n -> {
                    stockSelectedVoList.add(
                            stockSelectedAssembler.entityToVo(
                                    stockSelectedAssembler.doToEntity(n)
                            )
                    );
                }
        );
        return stockSelectedVoList;
    }
}
