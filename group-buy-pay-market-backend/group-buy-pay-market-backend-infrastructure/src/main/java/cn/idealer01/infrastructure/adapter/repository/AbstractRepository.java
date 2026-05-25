package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.infrastructure.dcc.DCCService;
import cn.idealer01.infrastructure.redis.IRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.function.Supplier;

public abstract class AbstractRepository {

    private final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    @Resource
    private IRedisService redisService;
    @Resource
    private DCCService dccService;

    protected <T> T getFromCacheOrDB(String cacheKey, Supplier<T> dbFallBack){
        // 是否开启缓存降级
        if(dccService.isCacheOpenSwitch()){
            //从缓存中获取
            T cacheValue = redisService.getValue(cacheKey);
            if(null != cacheValue){
                return cacheValue;
            }

            //缓存中没有从数据库中获取
            T dbValue = dbFallBack.get();
            if(null == dbValue){
                return null;
            }

            //将数据库中的值存入redis
            redisService.setValue(cacheKey, dbValue);

            return dbValue;
        }else{
            logger.warn("缓存降级：{}", cacheKey);
            return dbFallBack.get();
        }

    }

    //带过期时间的缓存
    protected <T> T getFromCacheOrDB(String cacheKey, Supplier<T> dbFallBack, long expired){
        // 是否开启缓存降级
        if(dccService.isCacheOpenSwitch()){
            //从缓存中获取
            T cacheValue = redisService.getValue(cacheKey);
            if(null != cacheValue){
                return cacheValue;
            }

            //缓存中没有从数据库中获取
            T dbValue = dbFallBack.get();
            if(null == dbValue){
                return null;
            }

            //将数据库中的值存入redis
            redisService.setValue(cacheKey, dbValue, expired);

            return dbValue;
        }else{
            logger.warn("缓存降级：{}", cacheKey);
            return dbFallBack.get();
        }

    }

}
