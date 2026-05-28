package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.activity.adapter.repository.IActivityRepository;
import cn.idealer01.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.idealer01.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.idealer01.domain.activity.model.valobj.SCSkuActivityVO;
import cn.idealer01.domain.activity.model.valobj.SkuVO;
import cn.idealer01.domain.activity.model.valobj.TeamStatisticVO;
import cn.idealer01.infrastructure.dao.*;
import cn.idealer01.infrastructure.dao.po.*;
import cn.idealer01.infrastructure.dcc.DCCService;
import cn.idealer01.infrastructure.redis.IRedisService;
import cn.idealer01.types.enums.DiscountTypeEnum;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ActivityRepository extends AbstractRepository implements IActivityRepository {
    @Resource
    private ISkuDao skuDao;
    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;
    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;
    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;
    @Resource
    private ISCSkuActivityDao scSkuActivityDao;
    @Resource
    private IRedisService redisService;
    @Resource
    private DCCService dccService;

    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId) {
        //查询合法的团购活动
        GroupBuyActivity groupBuyActivityRes = getFromCacheOrDB(GroupBuyActivity.cacheRedisKey(activityId), () ->
                groupBuyActivityDao.queryValidGroupBuyActivityId(activityId)
        );
        if(null == groupBuyActivityRes) return null;

        //根据活动中的折扣id查找到折扣信息
        String discountId = groupBuyActivityRes.getDiscountId();

        GroupBuyDiscount groupBuyDiscountRes = getFromCacheOrDB(GroupBuyDiscount.cacheRedisKey(discountId), () ->
            groupBuyDiscountDao.queryGroupBuyActivityDiscountByDiscountId(discountId)
        );
        if(null == groupBuyDiscountRes) return null;

        //构造活动折扣 VO对象
        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount = GroupBuyActivityDiscountVO.GroupBuyDiscount.builder()
                .discountName(groupBuyDiscountRes.getDiscountName())
                .discountDesc(groupBuyDiscountRes.getDiscountDesc())
                .discountType(DiscountTypeEnum.get(groupBuyDiscountRes.getDiscountType()))
                .marketPlan(groupBuyDiscountRes.getMarketPlan())
                .tagId(groupBuyDiscountRes.getTagId())
                .marketExpr(groupBuyDiscountRes.getMarketExpr())
                .build();

        return GroupBuyActivityDiscountVO.builder()
                .activityId(groupBuyActivityRes.getActivityId())
                .activityName(groupBuyActivityRes.getActivityName())
                .groupBuyDiscount(groupBuyDiscount)
                .groupType(groupBuyActivityRes.getGroupType())
                .endTime(groupBuyActivityRes.getEndTime())
                .target(groupBuyActivityRes.getTarget())
                .status(groupBuyActivityRes.getStatus())
                .validTime(groupBuyActivityRes.getValidTime())
                .startTime(groupBuyActivityRes.getStartTime())
                .takeLimitCount(groupBuyActivityRes.getTakeLimitCount())
                .tagScope(groupBuyActivityRes.getTagScope())
                .tagId(groupBuyActivityRes.getTagId())
                .build();
    }

    @Override
    public SkuVO querySkuByGoodsId(String goodsId) {
        Sku sku = skuDao.querySkuByGoodsId(goodsId);
        if(null == sku) return null;
        return SkuVO.builder()
                .goodsId(sku.getGoodsId())
                .goodsName(sku.getGoodsName())
                .originalPrice(sku.getOriginalPrice())
                .build();
    }

    @Override
    public SCSkuActivityVO querySCSkuActivityBySCGoodsId(String source, String channel, String goodsId) {
        SCSkuActivity scSkuActivityReq = new SCSkuActivity();
        scSkuActivityReq.setGoodsId(goodsId);
        scSkuActivityReq.setSource(source);
        scSkuActivityReq.setChannel(channel);

        SCSkuActivity scSkuActivity = scSkuActivityDao.querySCSkuActivityBySCGoodsId(scSkuActivityReq);
        if(null == scSkuActivity) return null;

        return SCSkuActivityVO.builder()
                .source(scSkuActivity.getSource())
                .chanel(scSkuActivity.getChannel())
                .goodsId(scSkuActivity.getGoodsId())
                .activityId(scSkuActivity.getActivityId())
                .build();
    }

    @Override
    public boolean isTagCrowdRange(String tagId, String userId) {
        if(tagId == null || tagId.isEmpty()) return true;
        RBitSet bitSet = redisService.getBitSet(tagId);
        if(!bitSet.isExists()) return true;
        return bitSet.get(redisService.getIndexFromUserId(userId));
    }

    @Override
    public boolean downgradeSwitch() {
        return dccService.isDowngradeSwitch();
    }

    @Override
    public boolean cutRange(String userId) {
        return dccService.isCutRange(userId);
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByOwner(Long activityId, String userId, Integer ownerCount) {
        //1.根据用户id，活动id，查询用户参与的拼团队伍
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setActivityId(activityId);
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setCount(ownerCount);
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByUserId(groupBuyOrderListReq);
        if(null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()) return null;

        //2. 过滤队伍获取TeamId
        Set<String> teamIds = groupBuyOrderLists.stream()
                .map(GroupBuyOrderList::getTeamId)
                .filter(teamId -> teamId != null && !teamId.isEmpty())
                .collect(Collectors.toSet());

        //3. 查询队伍明细，组装map结构
        List<GroupBuyOrder> groupBuyOrders = groupBuyOrderDao.queryGroupBuyProgressByTeamIds(teamIds);
        if(null == groupBuyOrders || groupBuyOrders.isEmpty()) return null;

        Map<String, GroupBuyOrder> groupBuyOrderMap = groupBuyOrders.stream()
                .collect(Collectors.toMap(GroupBuyOrder::getTeamId, order -> order));

        //4. 转换数据
        List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = new ArrayList<>();
        for (GroupBuyOrderList groupBuyOrderList : groupBuyOrderLists) {
            String teamId = groupBuyOrderList.getTeamId();
            GroupBuyOrder groupBuyOrder = groupBuyOrderMap.get(teamId);

            if(null == groupBuyOrder) continue;

            UserGroupBuyOrderDetailEntity userGroupBuyOrderDetailEntity = UserGroupBuyOrderDetailEntity.builder()
                    .userId(groupBuyOrderList.getUserId())
                    .teamId(groupBuyOrderList.getTeamId())
                    .activityId(groupBuyOrder.getActivityId())
                    .targetCount(groupBuyOrder.getTargetCount())
                    .completeCount(groupBuyOrder.getCompleteCount())
                    .lockCount(groupBuyOrder.getLockCount())
                    .outTradeNo(groupBuyOrderList.getOutTradeNo())
                    .validStartTime(groupBuyOrder.getValidStartTime())
                    .validEndTime(groupBuyOrder.getValidEndTime())
                    .build();

            userGroupBuyOrderDetailEntities.add(userGroupBuyOrderDetailEntity);
        }

        return userGroupBuyOrderDetailEntities;
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByRandom(Long activityId, String userId, Integer randomCount) {
        //1.根据用户id, 活动id， 查询用户参与的拼团队伍
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setActivityId(activityId);
        groupBuyOrderListReq.setCount(randomCount * 2);
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByRandom(groupBuyOrderListReq);
        if(null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()) return null;

        //总量大于randomCount，则随机提取randomCount个元素
        if(groupBuyOrderLists.size() > randomCount){
            // 随机打乱列表
            Collections.shuffle(groupBuyOrderLists);
            // 选取前randomCount个元素
            groupBuyOrderLists = groupBuyOrderLists.subList(0, randomCount);
        }

        //2.过滤队伍获取TeamId
        Set<String> teamIds = groupBuyOrderLists.stream()
                .map(GroupBuyOrderList::getTeamId)
                .filter(teamId -> teamId != null && !teamId.isEmpty())
                .collect(Collectors.toSet());

        //3.查询队伍明细，组装成Map结构
        List<GroupBuyOrder> groupBuyOrders = groupBuyOrderDao.queryGroupBuyProgressByTeamIds(teamIds);
        if(null == groupBuyOrders || groupBuyOrders.isEmpty()) return null;
        Map<String, GroupBuyOrder> groupBuyOrderMap = groupBuyOrders.stream()
                .collect(Collectors.toMap(GroupBuyOrder::getTeamId, order -> order));


        //4.转换数据
        List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = new ArrayList<>();
        for (GroupBuyOrderList groupBuyOrderList : groupBuyOrderLists) {
            String teamId = groupBuyOrderList.getTeamId();
            GroupBuyOrder groupBuyOrder = groupBuyOrderMap.get(teamId);
            if(null == groupBuyOrder) continue;

            UserGroupBuyOrderDetailEntity userGroupBuyOrderDetailEntity = UserGroupBuyOrderDetailEntity.builder()
                    .userId(groupBuyOrderList.getUserId())
                    .activityId(groupBuyOrder.getActivityId())
                    .teamId(groupBuyOrderList.getTeamId())
                    .targetCount(groupBuyOrder.getTargetCount())
                    .completeCount(groupBuyOrder.getCompleteCount())
                    .lockCount(groupBuyOrder.getLockCount())
                    .validEndTime(groupBuyOrder.getValidEndTime())
                    .validStartTime(groupBuyOrder.getValidStartTime())
                    .outTradeNo(groupBuyOrderList.getOutTradeNo())
                    .build();

            userGroupBuyOrderDetailEntities.add(userGroupBuyOrderDetailEntity);
        }


        return userGroupBuyOrderDetailEntities;
    }

    @Override
    public TeamStatisticVO queryTeamStatisticByActivity(Long activityId) {
        //1.根据活动id来查询拼团队伍
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByActivityId(activityId);

        if(null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()){
            return new TeamStatisticVO(0, 0, 0);
        }

        //2.过滤队伍获得所有参与活动的TeamId
        Set<String> teamIds = groupBuyOrderLists.stream()
                .map(GroupBuyOrderList::getTeamId)
                .filter(teamId -> teamId != null && !teamId.isEmpty())
                .collect(Collectors.toSet());

        //3.统计数据
        Integer allTeamCount = groupBuyOrderDao.queryAllTeamCount(teamIds);
        Integer allTeamCompleteCount = groupBuyOrderDao.queryAllTeamCompleteCount(teamIds);
        Integer allTeamUserCount = groupBuyOrderDao.queryAllTeamUserCount(teamIds);

        return TeamStatisticVO.builder()
                .allTeamCount(allTeamCount)
                .allTeamCompleteCount(allTeamCompleteCount)
                .allTeamUserCount(allTeamUserCount)
                .build();
    }
}
