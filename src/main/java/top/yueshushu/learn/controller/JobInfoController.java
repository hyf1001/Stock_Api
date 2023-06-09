package top.yueshushu.learn.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yueshushu.learn.annotation.AuthToken;
import top.yueshushu.learn.business.JobInfoBusiness;
import top.yueshushu.learn.business.UserBusiness;
import top.yueshushu.learn.common.ResultCode;
import top.yueshushu.learn.enumtype.DataFlagType;
import top.yueshushu.learn.mode.ro.JobInfoRo;
import top.yueshushu.learn.response.OutputResult;
import top.yueshushu.learn.util.CronExpression;
import top.yueshushu.learn.util.ThreadLocalUtils;

import javax.annotation.Resource;

/**
 * <p>
 * 定时任务处理
 * </p>
 *
 * @author 岳建立
 * @date 2022-01-02
 */
@RestController
@RequestMapping("/jobInfo")
@Api("定时任务处理")
public class JobInfoController extends BaseController {
    @Resource
    private JobInfoBusiness jobInfoBusiness;
    @Resource
    private UserBusiness userBusiness;

    @PostMapping("/list")
    @ApiOperation("查询任务信息")
    public OutputResult list(@RequestBody JobInfoRo jobInfoRo) {
        return jobInfoBusiness.listJob(jobInfoRo);
    }

    @PostMapping("/disable")
    @ApiOperation("禁用")
    @AuthToken
    public OutputResult disable(@RequestBody JobInfoRo jobInfoRo) {
        if (jobInfoRo.getId() == null) {
            return OutputResult.buildAlert(ResultCode.ID_IS_EMPTY);
        }
        if (!userBusiness.isSuperUser(ThreadLocalUtils.getUser())) {
            return OutputResult.buildFail(ResultCode.NO_AUTH);
        }
        return jobInfoBusiness.changeStatus(jobInfoRo.getId(), DataFlagType.DELETE);
    }

    @PostMapping("/enable")
    @ApiOperation("启用")
    @AuthToken
    public OutputResult enable(@RequestBody JobInfoRo jobInfoRo) {
        if (jobInfoRo.getId() == null) {
            return OutputResult.buildAlert(ResultCode.ID_IS_EMPTY);
        }
        if (!userBusiness.isSuperUser(ThreadLocalUtils.getUser())) {
            return OutputResult.buildFail(ResultCode.NO_AUTH);
        }
        return jobInfoBusiness.changeStatus(jobInfoRo.getId(), DataFlagType.NORMAL);
    }

    @PostMapping("/delete")
    @ApiOperation("删除定时任务")
    @AuthToken
    public OutputResult delete(@RequestBody JobInfoRo jobInfoRo) {
        if (jobInfoRo.getId() == null) {
            return OutputResult.buildAlert(ResultCode.ID_IS_EMPTY);
        }
        if (!userBusiness.isSuperUser(ThreadLocalUtils.getUser())) {
            return OutputResult.buildFail(ResultCode.NO_AUTH);
        }
        return jobInfoBusiness.deleteById(jobInfoRo.getId());
    }

    @PostMapping("/handler")
    @ApiOperation("手动执行定时任务")
    @AuthToken
    public OutputResult handler(@RequestBody JobInfoRo jobInfoRo) {
        if (jobInfoRo.getId() == null) {
            return OutputResult.buildAlert(ResultCode.ID_IS_EMPTY);
        }
        if (!userBusiness.isSuperUser(ThreadLocalUtils.getUser())) {
            return OutputResult.buildFail(ResultCode.NO_AUTH);
        }
        return jobInfoBusiness.handlerById(jobInfoRo.getId());
    }

    @PostMapping("/changeCron")
    @ApiOperation("修改cron 表达式")
    @AuthToken
    public OutputResult changeCron(@RequestBody JobInfoRo jobInfoRo) {
        if (jobInfoRo.getId() == null || StringUtils.isEmpty(jobInfoRo.getCron())) {
            return OutputResult.buildAlert(ResultCode.ID_IS_EMPTY);
        }
        boolean validExpression = CronExpression.isValidExpression(jobInfoRo.getCron());
        if (!validExpression) {
            return OutputResult.buildAlert(ResultCode.CRON_NO_VALID);
        }
        if (!userBusiness.isSuperUser(ThreadLocalUtils.getUser())) {
            return OutputResult.buildFail(ResultCode.NO_AUTH);
        }
        return jobInfoBusiness.changeCron(jobInfoRo.getId(), jobInfoRo.getCron());
    }
}
