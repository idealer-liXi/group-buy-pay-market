package cn.idealer01.domain.activity.service.trial.thread;

import cn.idealer01.domain.activity.adapter.repository.IActivityRepository;
import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.domain.activity.model.valobj.SCSkuActivityVO;
import lombok.AllArgsConstructor;
import java.util.concurrent.Callable;

@AllArgsConstructor
public class QueryGroupBuyActivityVOThreadTask implements Callable<GroupBuyActivityDiscountVO> {
    private final Long activityId;
    private final String source;
    private final String channel;
    private final String goodsId;
    private final IActivityRepository activityRepository;
    @Override
    public GroupBuyActivityDiscountVO call() throws Exception {
        //判断是否存在可用的活动
        Long availableActivityId = activityId;
        if(null == activityId){
            SCSkuActivityVO scSkuActivityVO =  activityRepository.querySCSkuActivityBySCGoodsId(source, channel, goodsId);
            if(null == scSkuActivityVO) return null;
            availableActivityId = scSkuActivityVO.getActivityId();
        }
        return activityRepository.queryGroupBuyActivityDiscountVO(availableActivityId);
    }
}
