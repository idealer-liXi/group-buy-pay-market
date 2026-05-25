package cn.idealer01.domain.activity.service;

import cn.idealer01.domain.activity.model.entity.MarketProductEntity;
import cn.idealer01.domain.activity.model.entity.TrialBalanceEntity;
import cn.idealer01.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.idealer01.domain.activity.model.valobj.TeamStatisticVO;

import java.util.List;

/**
 * 首页营销服务接口
 */
public interface IIndexGroupBuyMarketService {

    /**
     * 首页商品折扣计算
     * @param marketProductEntity
     * @return 商品折扣信息
     */
    TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity) throws Exception;

    /**
     * 查询正在进行中的拼团订单
     * @param activityId 活动
     * @param userId 个人
     * @param ownerCount 个人数量
     * @param randomCount 随机数量
     * @return 拼团详细信息
     */
    List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailList(Long activityId, String userId, Integer ownerCount, Integer randomCount);

    /**
     * 查询拼团数量信息
     * @param activityId 活动id
     * @return 拼团数据信息
     */
    TeamStatisticVO queryTeamStatisticByActivity(Long activityId);
}
