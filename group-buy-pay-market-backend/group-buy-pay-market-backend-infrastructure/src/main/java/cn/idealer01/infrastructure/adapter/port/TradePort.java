package cn.idealer01.infrastructure.adapter.port;

import cn.idealer01.domain.trade.adapter.port.ITradePort;
import cn.idealer01.domain.trade.model.entity.NotifyTaskEntity;
import cn.idealer01.domain.trade.model.valobj.NotifyTypeEnumVO;
import cn.idealer01.infrastructure.event.EventPublisher;
import cn.idealer01.infrastructure.gateway.GroupBuyNotifyService;
import cn.idealer01.infrastructure.redis.IRedisService;
import cn.idealer01.types.enums.NotifyTaskHTTPEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TradePort implements ITradePort {
    @Resource
    private GroupBuyNotifyService groupBuyNotifyService;
    @Resource
    private IRedisService redisService;
    @Resource
    private EventPublisher publisher;

    @Override
    public String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception {
        //服务部署在多个服务器，任务有可能被多个服务器调用，故需要对每个任务加锁,避免多次被执行
        RLock lock = redisService.getLock(notifyTask.lockKey());
        try{
            // waitTime = 3，3s内未获得锁则自动放弃
            // leaseTime = 0, 使用默认的30s的锁租约，启动看门狗续期机制，每10s检查当前线程是否拥有锁，若拥有自动加长租期
            // 抢到了锁
            if(lock.tryLock(3, 0, TimeUnit.SECONDS)){
                try {
                    if(NotifyTypeEnumVO.HTTP.getCode().equals(notifyTask.getNotifyType())){
                        //无效的notify_url则只需直接返回成功
                        if (StringUtils.isBlank(notifyTask.getNotifyUrl()) || "暂无".equals(notifyTask.getNotifyUrl())) {
                            return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
                        }
                        //发送Http回调请求
                        return groupBuyNotifyService.groupBuyNotify(notifyTask.getNotifyUrl(), notifyTask.getParameterJson());
                    }

                    if(NotifyTypeEnumVO.MQ.getCode().equals(notifyTask.getNotifyType())){
                        publisher.publish(notifyTask.getNotifyMQ(), notifyTask.getParameterJson());
                        return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
                    }

                }finally {
                    //释放锁，如果当前锁已经被持有&&被当前线程所持有
                    if(lock.isLocked() && lock.isHeldByCurrentThread()){
                        lock.unlock();
                    }
                }
            }

            //没有抢到锁，任务已经被其他线程执行
            return NotifyTaskHTTPEnumVO.NULL.getCode();
        }catch (Exception e){
            //出现异常，打断当前进程
            Thread.currentThread().interrupt();
            return NotifyTaskHTTPEnumVO.NULL.getCode();
        }

    }
}
