package cn.idealer01.domain.activity.service.trial.thread;

import cn.idealer01.domain.activity.adapter.repository.IActivityRepository;
import cn.idealer01.domain.activity.model.valobj.SkuVO;
import lombok.AllArgsConstructor;

import java.util.concurrent.Callable;

@AllArgsConstructor
public class QuerySkuVOFromDBThreadTask implements Callable<SkuVO> {
    private final String goodsId;
    private final IActivityRepository activityRepository;
    @Override
    public SkuVO call() throws Exception {
        return activityRepository.querySkuByGoodsId(goodsId);
    }
}
