package cn.idealer01.domain.trade.service.task;

import cn.idealer01.domain.trade.adapter.port.ITradePort;
import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.entity.NotifyTaskEntity;
import cn.idealer01.domain.trade.service.ITradeTaskService;
import cn.idealer01.types.enums.NotifyTaskHTTPEnumVO;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TradeTaskService implements ITradeTaskService {

    @Resource
    private ITradeRepository repository;
    @Resource
    private ITradePort port;

    @Override
    public Map<String, Integer> execNotifyJob() throws Exception {
        log.info("拼团交易-执行回调通知任务");
        //只会查询到 状态为未完成和重试的任务
        List<NotifyTaskEntity> notifyTaskEntityList =  repository.queryUnExecutedNotifyTaskList();
        return execSettlementNotifyJob(notifyTaskEntityList);
    }

    @Override
    public Map<String, Integer> execNotifyJob(String teamId) throws Exception {
        log.info("拼团交易-执行回调通知任务，指定 teamId:{}", teamId);
        //只会查询到 状态为未完成和重试的任务
        List<NotifyTaskEntity> notifyTaskEntityList =  repository.queryUnExecutedNotifyTaskList(teamId);
        return execSettlementNotifyJob(notifyTaskEntityList);
    }

    @Override
    public Map<String, Integer> execNotifyJob(NotifyTaskEntity notifyTaskEntity) throws Exception {
        log.info("拼团交易-执行回调通知回调，指定 teamId:{} notifyTaskEntity:{}", notifyTaskEntity.getTeamId(), JSON.toJSONString(notifyTaskEntity));
        return execSettlementNotifyJob(Collections.singletonList(notifyTaskEntity));
    }

    private Map<String, Integer> execSettlementNotifyJob(List<NotifyTaskEntity> notifyTaskEntityList) throws Exception {
        int successCount = 0, errorCount = 0, retryCount = 0;
        for (NotifyTaskEntity notifyTaskEntity : notifyTaskEntityList) {
            //进行回调执行，获得任务执行结果
            String response = port.groupBuyNotify(notifyTaskEntity);

            if(NotifyTaskHTTPEnumVO.SUCCESS.getCode().equals(response)){
                //任务执行成功，更新数据库状态
                int updateCount = repository.updateNotifyTaskStatusSuccess(notifyTaskEntity);
                if(updateCount == 1){
                    successCount += 1;
                }
            }else if (NotifyTaskHTTPEnumVO.ERROR.getCode().equals(response)){
                //回调次数 > 5 更新回调任务状态为失败
                if(notifyTaskEntity.getNotifyCount() > 5){
                    int updateCount = repository.updateNotifyTaskStatusError(notifyTaskEntity);
                    if(updateCount == 1){
                        errorCount += 1;
                    }
                }else{
                    //回调次数 < 5次，继续重试，更新状态为重试
                    int updateCount = repository.updateNotifyTaskStatusRetry(notifyTaskEntity);
                    if(updateCount == 1){
                        retryCount += 1;
                    }
                }
            }
        }

        Map<String, Integer> resultMap = new HashMap<>();
        //需要进行回调的任务个数
        resultMap.put("waitCount", notifyTaskEntityList.size());
        //回调成功的任务个数
        resultMap.put("successCount", successCount);
        //回调失败的任务个数
        resultMap.put("errorCount", errorCount);
        //处于回调重试的任务个数
        resultMap.put("retryCount", retryCount);

        return resultMap;
    }
}
