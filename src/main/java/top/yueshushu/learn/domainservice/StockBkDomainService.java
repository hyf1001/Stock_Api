package top.yueshushu.learn.domainservice;

import com.baomidou.mybatisplus.extension.service.IService;
import top.yueshushu.learn.domain.StockBkDo;
import top.yueshushu.learn.enumtype.BKType;

import java.util.List;

/**
 * <p>
 * 股票版块信息 服务类
 * </p>
 *
 * @author 岳建立
 * @since 2023-02-07
 */
public interface StockBkDomainService extends IService<StockBkDo> {
    /**
     * 查询版块列表信息
     *
     * @param bkType
     */
    List<StockBkDo> listByOrder(BKType bkType);

    /**
     * 根据版块编码查询版块的记录信息
     *
     * @param code 版块编码
     */
    StockBkDo getByCode(String code);

    List<StockBkDo> listByCodes(List<String> bkCodeList);
}
