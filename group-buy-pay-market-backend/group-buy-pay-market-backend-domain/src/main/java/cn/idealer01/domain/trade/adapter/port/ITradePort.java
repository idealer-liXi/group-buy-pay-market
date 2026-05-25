package cn.idealer01.domain.trade.adapter.port;

import cn.idealer01.domain.trade.model.entity.NotifyTaskEntity;

public interface ITradePort {
    String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception;
}
