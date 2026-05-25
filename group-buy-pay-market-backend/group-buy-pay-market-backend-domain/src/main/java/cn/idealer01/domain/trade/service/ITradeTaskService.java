package cn.idealer01.domain.trade.service;

import cn.idealer01.domain.trade.model.entity.NotifyTaskEntity;

import java.util.Map;

public interface ITradeTaskService {

    Map<String, Integer> execNotifyJob() throws Exception;

    Map<String, Integer> execNotifyJob(String teamId) throws Exception;

    Map<String, Integer> execNotifyJob(NotifyTaskEntity notifyTaskEntity) throws Exception;

}
