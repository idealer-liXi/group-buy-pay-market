package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import cn.idealer01.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import cn.idealer01.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import cn.idealer01.domain.trade.model.entity.*;
import cn.idealer01.domain.trade.model.valobj.*;
import cn.idealer01.infrastructure.dao.IGroupBuyActivityDao;
import cn.idealer01.infrastructure.dao.IGroupBuyOrderDao;
import cn.idealer01.infrastructure.dao.IGroupBuyOrderListDao;
import cn.idealer01.infrastructure.dao.INotifyTaskDao;
import cn.idealer01.infrastructure.dao.IOrderDao;
import cn.idealer01.infrastructure.dao.po.GroupBuyActivity;
import cn.idealer01.infrastructure.dao.po.GroupBuyOrder;
import cn.idealer01.infrastructure.dao.po.GroupBuyOrderList;
import cn.idealer01.infrastructure.dao.po.NotifyTask;
import cn.idealer01.infrastructure.dcc.DCCService;
import cn.idealer01.infrastructure.redis.IRedisService;
import cn.idealer01.types.common.Constants;
import cn.idealer01.types.enums.ActivityStatusEnumVO;
import cn.idealer01.types.enums.GroupBuyOrderEnumVO;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TradeRepository implements ITradeRepository {
    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;
    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;
    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private INotifyTaskDao notifyTaskDao;
    @Resource
    private IOrderDao orderDao;
    @Resource
    private DCCService dccService;
    @Value("${spring.rabbitmq.config.producer.topic_team_success.routing_key}")
    private String topic_team_success;
    @Value("${spring.rabbitmq.config.producer.topic_team_refund.routing_key}")
    private String topic_team_refund;
    @Resource
    private IRedisService redisService;

    @Override
    public MarketPayOrderEntity queryMarketPayOrderEntityByOutTradeNo(String userId, String outTradeNo) {
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setOutTradeNo(outTradeNo);

        GroupBuyOrderList groupBuyOrderListRes = groupBuyOrderListDao.queryGroupBuyOrderRecordByOutTradeNo(groupBuyOrderListReq);
        if(null == groupBuyOrderListRes) return null;

        return MarketPayOrderEntity.builder()
                .teamId(groupBuyOrderListRes.getTeamId())
                .orderId(groupBuyOrderListRes.getOrderId())
                .originalPrice(groupBuyOrderListRes.getOriginalPrice())
                .deductionPrice(groupBuyOrderListRes.getDeductionPrice())
                .payPrice(groupBuyOrderListRes.getPayPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.valueOf(groupBuyOrderListRes.getStatus()))
                .build();
    }

    /**
     * 执行时间超过500秒回滚
     * @param groupBuyOrderAggregate
     * @return
     */
    @Transactional(timeout = 500)
    @Override
    public MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate) {
        //聚合对象信息
        UserEntity userEntity = groupBuyOrderAggregate.getUserEntity();
        PayActivityEntity payActivityEntity = groupBuyOrderAggregate.getPayActivityEntity();
        PayDiscountEntity payDiscountEntity = groupBuyOrderAggregate.getPayDiscountEntity();
        Integer userTakeOrderCount = groupBuyOrderAggregate.getUserTakeOrderCount();
        NotifyConfigVO notifyConfigVO = payDiscountEntity.getNotifyConfigVO();

        //判断是否有团，空则创建新团
        String teamId = payActivityEntity.getTeamId();
        if(StringUtils.isBlank(teamId)){
            //随机生成拼团号
            teamId = RandomStringUtils.randomNumeric(8);

            //日期处理
            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.MINUTE, payActivityEntity.getValidTime());

            // 构建拼团订单
            GroupBuyOrder groupBuyOrder = GroupBuyOrder.builder()
                    .teamId(teamId)
                    .activityId(payActivityEntity.getActivityId())
                    .source(payDiscountEntity.getSource())
                    .channel(payDiscountEntity.getChannel())
                    .originalPrice(payDiscountEntity.getOriginalPrice())
                    .deductionPrice(payDiscountEntity.getDeductionPrice())
                    .payPrice(payDiscountEntity.getPayPrice())
                    .targetCount(payActivityEntity.getTargetCount())
                    .completeCount(0)
                    .lockCount(1)
                    .validStartTime(currentDate)
                    .validEndTime(calendar.getTime())
                    .notifyType(notifyConfigVO.getNotifyType().getCode())
                    .notifyUrl(notifyConfigVO.getNotifyUrl())
                    .build();

            //写入记录
            groupBuyOrderDao.insert(groupBuyOrder);
        } else {
            //已有拼团，进行锁单
            int updateAddLockCount = groupBuyOrderDao.updateAddLockCount(teamId);
            //锁单失败
            if(1 != updateAddLockCount){
                throw new AppException(ResponseCode.E0005.getCode(), ResponseCode.E0005.getInfo());
            }

        }

        //创建拼团记录
        String orderId = RandomStringUtils.randomNumeric(12);
        GroupBuyOrderList groupBuyOrderListReq = GroupBuyOrderList.builder()
                .userId(userEntity.getUserId())
                .teamId(teamId)
                .orderId(orderId)
                .activityId(payActivityEntity.getActivityId())
                .startTime(payActivityEntity.getStartTime())
                .endTime(payActivityEntity.getEndTime())
                .goodsId(payDiscountEntity.getGoodsId())
                .source(payDiscountEntity.getSource())
                .channel(payDiscountEntity.getChannel())
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .payPrice(payDiscountEntity.getPayPrice())
                .status(TradeOrderStatusEnumVO.CREATE.getCode())
                .outTradeNo(payDiscountEntity.getOutTradeNo())
                .bizId(payActivityEntity.getActivityId() + Constants.UNDERLINE + userEntity.getUserId() + Constants.UNDERLINE + (userTakeOrderCount + 1))
                .build();

        try {
            //写入拼团记录
            groupBuyOrderListDao.insert(groupBuyOrderListReq);
        }catch (DuplicateKeyException e){
            throw new AppException(ResponseCode.INDEX_EXCEPTION.getCode(), ResponseCode.INDEX_EXCEPTION.getInfo());
        }

        return MarketPayOrderEntity.builder()
                .orderId(orderId)
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .payPrice(payDiscountEntity.getPayPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.CREATE)
                .teamId(teamId)
                .build();
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyProgress(teamId);
        if(null == groupBuyOrder) return null;
        return GroupBuyProgressVO.builder()
                .completeCount(groupBuyOrder.getCompleteCount())
                .targetCount(groupBuyOrder.getTargetCount())
                .lockCount(groupBuyOrder.getLockCount())
                .build();
    }

    @Override
    public GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId) {
        GroupBuyActivity groupBuyActivity = groupBuyActivityDao.queryGroupBuyActivityByActivityId(activityId);
        return GroupBuyActivityEntity.builder()
                .activityId(groupBuyActivity.getActivityId())
                .activityName(groupBuyActivity.getActivityName())
                .discountId(groupBuyActivity.getDiscountId())
                .groupType(groupBuyActivity.getGroupType())
                .takeLimitCount(groupBuyActivity.getTakeLimitCount())
                .target(groupBuyActivity.getTarget())
                .validTime(groupBuyActivity.getValidTime())
                .status(ActivityStatusEnumVO.valueOf(groupBuyActivity.getStatus()))
                .startTime(groupBuyActivity.getStartTime())
                .endTime(groupBuyActivity.getEndTime())
                .tagId(groupBuyActivity.getTagId())
                .tagScope(groupBuyActivity.getTagScope())
                .build();
    }

    @Override
    public Integer queryOrderCountByActivityId(Long activityId, String userId) {
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setActivityId(activityId);
        groupBuyOrderListReq.setUserId(userId);

        return groupBuyOrderListDao.queryOrderCountByActivityId(groupBuyOrderListReq);

    }

    @Override
    public GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyTeamByTeamId(teamId);
        return GroupBuyTeamEntity.builder()
                .teamId(teamId)
                .activityId(groupBuyOrder.getActivityId())
                .targetCount(groupBuyOrder.getTargetCount())
                .completeCount(groupBuyOrder.getCompleteCount())
                .lockCount(groupBuyOrder.getLockCount())
                .status(GroupBuyOrderEnumVO.valueOf(groupBuyOrder.getStatus()))
                .validStartTime(groupBuyOrder.getValidStartTime())
                .validEndTime(groupBuyOrder.getValidEndTime())
                .notifyConfigVO(NotifyConfigVO.builder()
                        .notifyType(NotifyTypeEnumVO.valueOf(groupBuyOrder.getNotifyType()))
                        .notifyUrl(groupBuyOrder.getNotifyUrl())
                        .notifyMQ(topic_team_success)
                        .build())
                .build();
    }

    @Transactional(timeout = 500)
    @Override
    public NotifyTaskEntity settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate) {

        GroupBuyTeamEntity groupBuyTeamEntity = groupBuyTeamSettlementAggregate.getGroupBuyTeamEntity();
        NotifyConfigVO notifyConfigVO = groupBuyTeamEntity.getNotifyConfigVO();
        TradePaySuccessEntity tradePaySuccessEntity = groupBuyTeamSettlementAggregate.getTradePaySuccessEntity();
        UserEntity userEntity = groupBuyTeamSettlementAggregate.getUserEntity();

        //1.更新拼团订单明细，用户完成订单
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userEntity.getUserId());
        groupBuyOrderListReq.setOutTradeNo(tradePaySuccessEntity.getOutTradeNo());
        groupBuyOrderListReq.setOutTradeTime(tradePaySuccessEntity.getOutTradeTime());

        int updateOrderListStatusCount = groupBuyOrderListDao.updateOrderStatus2COMPLETE(groupBuyOrderListReq);
        if(1 != updateOrderListStatusCount){
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        //2.更新拼团订单达成数量
        int updateAddCount = groupBuyOrderDao.updateAddCompleteCount(groupBuyTeamEntity.getTeamId());
        if(1 != updateAddCount){
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        //3.更新拼团完成状态
        if(groupBuyTeamEntity.getTargetCount() - groupBuyTeamEntity.getCompleteCount() == 1){
            int updateOrderStatusCount = groupBuyOrderDao.updateOrderStatus2COMPLETE(groupBuyTeamEntity.getTeamId());
            if(1 != updateOrderStatusCount){
                throw new AppException(ResponseCode.UPDATE_ZERO);
            }

            //查询拼团交易完成的外部单号列表
            List<String> outTradeNoList = groupBuyOrderListDao.queryGroupBuyCompleteOrderOutTradeNoListTeamId(groupBuyTeamEntity.getTeamId());

            //拼团完成写入回调任务记录
            NotifyTask notifyTask = new NotifyTask();
            notifyTask.setActivityId(groupBuyTeamEntity.getActivityId());
            notifyTask.setTeamId(groupBuyTeamEntity.getTeamId());
            notifyTask.setNotifyCategory(TaskNotifyCategoryEnumVO.TRADE_SETTLEMENT.getCode());
            notifyTask.setNotifyType(notifyConfigVO.getNotifyType().getCode());
            notifyTask.setNotifyUrl(NotifyTypeEnumVO.HTTP.equals(notifyConfigVO.getNotifyType())? notifyConfigVO.getNotifyUrl() : null);
            notifyTask.setNotifyMQ(NotifyTypeEnumVO.MQ.equals(notifyConfigVO.getNotifyType())? notifyConfigVO.getNotifyMQ() : null);
            notifyTask.setNotifyCount(0);
            notifyTask.setNotifyStatus(0);
            notifyTask.setUuid(groupBuyTeamEntity.getTeamId() + Constants.UNDERLINE + TaskNotifyCategoryEnumVO.TRADE_SETTLEMENT.getCode() + Constants.UNDERLINE + tradePaySuccessEntity.getOutTradeNo());

            Map<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("teamId", groupBuyTeamEntity.getTeamId());
            parameterMap.put("outTradeNoList", outTradeNoList);
            notifyTask.setParameterJson(JSON.toJSONString(parameterMap));

            notifyTaskDao.insert(notifyTask);

            return NotifyTaskEntity.builder()
                    .teamId(notifyTask.getTeamId())
                    .notifyType(notifyTask.getNotifyType())
                    .notifyMQ(notifyConfigVO.getNotifyMQ())
                    .notifyUrl(notifyConfigVO.getNotifyUrl())
                    .parameterJson(notifyTask.getParameterJson())
                    .notifyCount(notifyTask.getNotifyCount())
                    .uuid(notifyTask.getUuid())
                    .build();
        }

        return null;
    }

    @Override
    public boolean isSCBlackIntercept(String source, String channel) {
        return dccService.isSCBlackIntercept(source, channel);
    }

    @Override
    public List<NotifyTaskEntity> queryUnExecutedNotifyTaskList() {
        List<NotifyTask> notifyTaskList = notifyTaskDao.queryUnExecutedNotifyTaskList();
        if(notifyTaskList.isEmpty()) return new ArrayList<>();

        List<NotifyTaskEntity> notifyTaskEntityList = new ArrayList<>();
        for (NotifyTask notifyTask : notifyTaskList) {
            notifyTaskEntityList.add(NotifyTaskEntity.builder()
                            .teamId(notifyTask.getTeamId())
                            .notifyCount(notifyTask.getNotifyCount())
                            .notifyType(notifyTask.getNotifyType())
                            .notifyMQ(notifyTask.getNotifyMQ())
                            .notifyUrl(notifyTask.getNotifyUrl())
                            .uuid(notifyTask.getUuid())
                            .parameterJson(notifyTask.getParameterJson())
                    .build());
        }

        return notifyTaskEntityList;
    }

    @Override
    public List<NotifyTaskEntity> queryUnExecutedNotifyTaskList(String teamId) {
        NotifyTask notifyTask = notifyTaskDao.queryUnExecutedNotifyTaskByTeamId(teamId);
        if(notifyTask == null) return new ArrayList<>();
        return Collections.singletonList(NotifyTaskEntity.builder()
                        .teamId(notifyTask.getTeamId())
                        .notifyCount(notifyTask.getNotifyCount())
                        .notifyType(notifyTask.getNotifyType())
                        .notifyMQ(notifyTask.getNotifyMQ())
                        .notifyUrl(notifyTask.getNotifyUrl())
                        .uuid(notifyTask.getUuid())
                        .parameterJson(notifyTask.getParameterJson())
                .build());
    }

    @Override
    public int updateNotifyTaskStatusSuccess(NotifyTaskEntity notifyTaskEntity) {
        NotifyTask notifyTask = NotifyTask.builder()
                .teamId(notifyTaskEntity.getTeamId())
                .uuid(notifyTaskEntity.getUuid())
                .build();
        return notifyTaskDao.updateNotifyTaskStatusSuccess(notifyTask);
    }

    @Override
    public int updateNotifyTaskStatusError(NotifyTaskEntity notifyTaskEntity) {
        NotifyTask notifyTask = NotifyTask.builder()
                .teamId(notifyTaskEntity.getTeamId())
                .uuid(notifyTaskEntity.getUuid())
                .build();
        return notifyTaskDao.updateNotifyTaskStatusError(notifyTask);
    }

    @Override
    public int updateNotifyTaskStatusRetry(NotifyTaskEntity notifyTaskEntity) {
        NotifyTask notifyTask = NotifyTask.builder()
                .teamId(notifyTaskEntity.getTeamId())
                .uuid(notifyTaskEntity.getUuid())
                .build();
        return notifyTaskDao.updateNotifyTaskStatusRetry(notifyTask);
    }

    //在redis中先抢占锁
    @Override
    public boolean occupyTeamStock(String teamStockKey, String recoveryTeamStockKey, Integer target, Integer validTime) {
        Long recoveryCount = redisService.getAtomicLong(recoveryTeamStockKey);
        recoveryCount = recoveryCount == null ? 0 : recoveryCount;

        //领取号码
        long occupy = redisService.incr(teamStockKey) + 1;
        //领取的号码超过了目标
        if(occupy > target + recoveryCount){
            redisService.setAtomicLong(teamStockKey, target); //该team已经完成了目标
            return false; //占用失败
        }

        //存在领取了相同号码的情况，判断当前领取的号码是否被其他人领取
        String lockKey = teamStockKey + Constants.UNDERLINE + occupy;
        Boolean lock = redisService.setNx(lockKey, validTime + 60, TimeUnit.MINUTES);

        if(!lock){
            log.info("组队库存加锁失败 lockKey:{}",lockKey);
        }

        return lock;
    }

    @Override
    public void recoveryTeamStock(String recoveryTeamStockKey, Integer validTime) {
        if(null == recoveryTeamStockKey) return; //首个人开团不需要在redis中锁单，故他的recoveryTeamStockKey为空
        redisService.incr(recoveryTeamStockKey);
    }

    @Override
    public NotifyTaskEntity unpaid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate) {
        GroupBuyProgressVO groupBuyProgressVO = groupBuyRefundAggregate.getGroupBuyProgressVO();
        TradeRefundOrderEntity tradeRefundOrderEntity = groupBuyRefundAggregate.getTradeRefundOrderEntity();

        //1.根据userId,orderId 修改详细订单记录状态为0
        String userId = tradeRefundOrderEntity.getUserId();
        String orderId = tradeRefundOrderEntity.getOrderId();
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setOrderId(orderId);

        int updateUnpaid2RefundCount = groupBuyOrderListDao.unpaid2Refund(groupBuyOrderListReq);
        if(1 != updateUnpaid2RefundCount){
            log.error("逆向流程--unpaid2Refund，更新订单状态（退单）失败 userId:{} orderId:{}", userId, orderId);
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        orderDao.changeOrderClose(tradeRefundOrderEntity.getOutTradeNo());

        //2.根据teamId,lockCount修改订单锁单量
        String teamId = tradeRefundOrderEntity.getTeamId();
        GroupBuyOrder groupBuyOrderReq = new GroupBuyOrder();
        groupBuyOrderReq.setTeamId(teamId);
        groupBuyOrderReq.setLockCount(groupBuyProgressVO.getLockCount());

        int updateTeamUnpaid2RefundCount = groupBuyOrderDao.unpaid2Refund(groupBuyOrderReq);
        if(1 != updateTeamUnpaid2RefundCount){
            log.error("逆向流程--unpaid2Refund，更新组队记录（退单）失败 userId:{} orderId:{}", userId, orderId);
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        //3.插入回调任务
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("type", RefundTypeEnumVO.UNPAID_UNLOCK.getCode());
        parameterMap.put("teamId", teamId);
        parameterMap.put("userId", userId);
        parameterMap.put("activityId", tradeRefundOrderEntity.getActivityId());
        parameterMap.put("outTradeNo", tradeRefundOrderEntity.getOutTradeNo());
        parameterMap.put("orderId", orderId);

        NotifyTask notifyTask = NotifyTask.builder()
                .uuid(teamId + Constants.UNDERLINE + RefundTypeEnumVO.UNPAID_UNLOCK + Constants.UNDERLINE + orderId)
                .teamId(teamId)
                .activityId(tradeRefundOrderEntity.getActivityId())
                .notifyType(NotifyTypeEnumVO.MQ.getCode())
                .notifyMQ(topic_team_refund)
                .notifyCategory(TaskNotifyCategoryEnumVO.TRADE_UNPAID2REFUND.getCode())
                .notifyCount(0)
                .notifyStatus(0)
                .parameterJson(JSON.toJSONString(parameterMap))
                .build();

        notifyTaskDao.insert(notifyTask);

        //4.返回回调任务

        return NotifyTaskEntity.builder()
                .teamId(notifyTask.getTeamId())
                .notifyType(notifyTask.getNotifyType())
                .notifyMQ(notifyTask.getNotifyMQ())
                .notifyCount(notifyTask.getNotifyCount())
                .parameterJson(notifyTask.getParameterJson())
                .uuid(notifyTask.getUuid())
                .build();
    }

    @Override
    @Transactional(timeout = 5000)
    public NotifyTaskEntity paid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate) {
        //1.提取数据
        TradeRefundOrderEntity tradeRefundOrderEntity = groupBuyRefundAggregate.getTradeRefundOrderEntity();
        GroupBuyProgressVO groupBuyProgressVO = groupBuyRefundAggregate.getGroupBuyProgressVO();

        String userId = tradeRefundOrderEntity.getUserId();
        String orderId = tradeRefundOrderEntity.getOrderId();
        String teamId = tradeRefundOrderEntity.getTeamId();
        Long activityId = tradeRefundOrderEntity.getActivityId();
        //2.修改详细订单表
        GroupBuyOrderList groupBuyOrderListReq = GroupBuyOrderList.builder()
                .userId(userId)
                .orderId(orderId)
                .build();
        int updatePaid2RefundCount = groupBuyOrderListDao.paid2Refund(groupBuyOrderListReq);
        if(1 != updatePaid2RefundCount){
            log.error("逆向流程--paid2Refund，更新订单状态(退单)失败 userId:{} teamId:{}", userId, teamId);
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        //3.修改订单表
        GroupBuyOrder groupBuyOrderReq = GroupBuyOrder.builder()
                .teamId(teamId)
                .lockCount(groupBuyProgressVO.getLockCount())
                .completeCount(groupBuyProgressVO.getCompleteCount())
                .build();
        int updateTeamPaid2RefundCount = groupBuyOrderDao.paid2Refund(groupBuyOrderReq);
        if(1 != updateTeamPaid2RefundCount){
            log.error("逆向流程--paid2Refund, 更新组队状态(退单)失败 userId:{} teamId:{}", userId, teamId);
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        //4.构建回调任务
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("type", RefundTypeEnumVO.PAID_UNFORMED.getCode());
        parameterMap.put("userId", userId);
        parameterMap.put("teamId", teamId);
        parameterMap.put("orderId", orderId);
        parameterMap.put("activityId", activityId);
        parameterMap.put("outTradeNo", tradeRefundOrderEntity.getOutTradeNo());

        NotifyTask notifyTask = NotifyTask.builder()
                .uuid(tradeRefundOrderEntity.getTeamId() + Constants.UNDERLINE + TaskNotifyCategoryEnumVO.TRADE_PAID2REFUND + Constants.UNDERLINE + tradeRefundOrderEntity.getOrderId())
                .activityId(activityId)
                .teamId(teamId)
                .notifyType(NotifyTypeEnumVO.MQ.getCode())
                .notifyMQ(topic_team_refund)
                .notifyCategory(TaskNotifyCategoryEnumVO.TRADE_PAID2REFUND.getCode())
                .notifyCount(0)
                .notifyStatus(0)
                .parameterJson(JSON.toJSONString(parameterMap))
                .build();

        notifyTaskDao.insert(notifyTask);

        //5.返回回调信息
        return NotifyTaskEntity.builder()
                .teamId(notifyTask.getTeamId())
                .notifyType(notifyTask.getNotifyType())
                .notifyMQ(notifyTask.getNotifyMQ())
                .notifyCount(notifyTask.getNotifyCount())
                .parameterJson(notifyTask.getParameterJson())
                .uuid(notifyTask.getUuid())
                .build();
    }

    @Override
    @Transactional(timeout = 5000)
    public NotifyTaskEntity paidTeam2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate) {
        TradeRefundOrderEntity tradeRefundOrderEntity = groupBuyRefundAggregate.getTradeRefundOrderEntity();

        //1.更新详细记录
        String userId = tradeRefundOrderEntity.getUserId();
        String orderId = tradeRefundOrderEntity.getOrderId();
        String teamId = tradeRefundOrderEntity.getTeamId();
        Long activityId = tradeRefundOrderEntity.getActivityId();

        GroupBuyOrderList groupBuyOrderListReq = GroupBuyOrderList.builder()
                .userId(userId)
                .orderId(orderId)
                .build();

        int updatePaid2RefundCount = groupBuyOrderListDao.paidTeam2Refund(groupBuyOrderListReq);
        if(1 != updatePaid2RefundCount){
            log.error("逆向流程-paidTeam2Refund, 拼团退单失败 userId:{} orderId:{}", userId, orderId);
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        //2.关闭退单用户的支付订单，拼团队伍保持已完成状态。
        boolean closeOrderResult = orderDao.changeOrderClose(tradeRefundOrderEntity.getOutTradeNo());
        if(!closeOrderResult){
            log.error("逆向流程-paidTeam2Refund, 关闭支付订单失败 userId:{} outTradeNo:{}", userId, tradeRefundOrderEntity.getOutTradeNo());
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        //3.持久化通知消息记录
        NotifyTask notifyTask = NotifyTask.builder()
                .teamId(teamId)
                .activityId(activityId)
                .notifyType(NotifyTypeEnumVO.MQ.getCode())
                .notifyMQ(topic_team_refund)
                .notifyCategory(TaskNotifyCategoryEnumVO.TRADE_PAID_TEAM2REFUND.getCode())
                .notifyCount(0)
                .notifyStatus(0)
                .uuid(tradeRefundOrderEntity.getTeamId() + Constants.UNDERLINE + TaskNotifyCategoryEnumVO.TRADE_PAID_TEAM2REFUND.getCode() + Constants.UNDERLINE + tradeRefundOrderEntity.getOrderId())
                .build();

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("type", RefundTypeEnumVO.PAID_FORMED.getCode());
        parameterMap.put("userId", userId);
        parameterMap.put("teamId", teamId);
        parameterMap.put("orderId", orderId);
        parameterMap.put("activityId", activityId);
        parameterMap.put("outTradeNo", tradeRefundOrderEntity.getOutTradeNo());
        notifyTask.setParameterJson(JSON.toJSONString(parameterMap));

        notifyTaskDao.insert(notifyTask);

        //4.返回通知信息
        return NotifyTaskEntity.builder()
                .uuid(notifyTask.getUuid())
                .teamId(notifyTask.getTeamId())
                .notifyType(notifyTask.getNotifyType())
                .notifyMQ(notifyTask.getNotifyMQ())
                .notifyCount(notifyTask.getNotifyCount())
                .parameterJson(notifyTask.getParameterJson())
                .build();
    }

    @Override
    public void refund2AddRecovery(String recoveryTeamStockKey, String orderId) {
        //1.若值为空，直接返回
        if(StringUtils.isBlank(recoveryTeamStockKey) || StringUtils.isBlank(orderId)){
            return;
        }

        //2.使用orderId作为Key，避免同一订单多次恢复库存
        String lockKey = "Refund_lock_" + orderId;
        //3.获取分布式锁，时间为30天，不粘多次重复恢复库存
        Boolean lockAcquired = redisService.setNx(lockKey, 30 * 24 * 60, TimeUnit.MINUTES);
        if(!lockAcquired){
            log.warn("订单orderId:{}, 恢复库存锁单进行中......, 请勿重复", orderId);
            return;
        }

        try{
            redisService.incr(recoveryTeamStockKey);
            log.info("订单 {} 恢复库存成功，恢复库存key: {}", orderId, recoveryTeamStockKey);
        }catch (Exception e){
            log.error("订单 {} 恢复库存失败，恢复库存key: {}", orderId, recoveryTeamStockKey, e);
            //5.恢复库存失败，删除LockKey,允许MQ重新消费
            redisService.remove(lockKey);
            throw e;
        }


    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryTimeoutUnpaidOrderList() {
        //1.查询所有未支付的详细订单信息
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryTimeoutUnpaidOrderList();
        if(null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()){
            return new ArrayList<>();
        }

        //2.获取所有TeamId
        Set<String> teamIds = groupBuyOrderLists.stream()
                .map(GroupBuyOrderList::getTeamId)
                .collect(Collectors.toSet());

        //3.查询团队信息
        List<GroupBuyOrder> groupBuyOrders = groupBuyOrderDao.queryGroupBuyTeamByTeamIds(teamIds);
        if(null == groupBuyOrders || groupBuyOrders.isEmpty()){
            return new ArrayList<>();
        }

        Map<String, GroupBuyOrder> groupBuyOrderMap = groupBuyOrders.stream()
                .collect(Collectors.toMap(GroupBuyOrder::getTeamId, order -> order));

        //4.转换数据汇总
        List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = new ArrayList<>();
        for (GroupBuyOrderList groupBuyOrderList : groupBuyOrderLists) {
            String teamId = groupBuyOrderList.getTeamId();
            GroupBuyOrder groupBuyOrder = groupBuyOrderMap.get(teamId);
            if (null == groupBuyOrder) continue;

            UserGroupBuyOrderDetailEntity userGroupBuyOrderDetailEntity = UserGroupBuyOrderDetailEntity.builder()
                    .userId(groupBuyOrderList.getUserId())
                    .teamId(groupBuyOrder.getTeamId())
                    .activityId(groupBuyOrder.getActivityId())
                    .targetCount(groupBuyOrder.getTargetCount())
                    .completeCount(groupBuyOrder.getCompleteCount())
                    .lockCount(groupBuyOrder.getLockCount())
                    .validStartTime(groupBuyOrder.getValidStartTime())
                    .validEndTime(groupBuyOrder.getValidEndTime())
                    .outTradeNo(groupBuyOrderList.getOutTradeNo())
                    .source(groupBuyOrderList.getSource())
                    .channel(groupBuyOrderList.getChannel())
                    .build();

            userGroupBuyOrderDetailEntities.add(userGroupBuyOrderDetailEntity);
        }

        return userGroupBuyOrderDetailEntities;
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryTimeoutPaidUnformedOrderList() {
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryTimeoutPaidUnformedOrderList();
        if (null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()) {
            return new ArrayList<>();
        }

        List<UserGroupBuyOrderDetailEntity> result = new ArrayList<>();
        for (GroupBuyOrderList orderList : groupBuyOrderLists) {
            result.add(UserGroupBuyOrderDetailEntity.builder()
                    .userId(orderList.getUserId())
                    .teamId(orderList.getTeamId())
                    .activityId(orderList.getActivityId())
                    .source(orderList.getSource())
                    .channel(orderList.getChannel())
                    .outTradeNo(orderList.getOutTradeNo())
                    .build());
        }
        return result;
    }
}
