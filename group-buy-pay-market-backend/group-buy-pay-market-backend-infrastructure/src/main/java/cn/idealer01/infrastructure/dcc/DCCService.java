package cn.idealer01.infrastructure.dcc;

import cn.idealer.wrench.dynamic.config.center.type.anntations.DCCValue;
import cn.idealer01.types.common.Constants;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DCCService {
    /**
     * 降级开关: 0关闭，1开启
     */
    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;

    @DCCValue("cutRange:100")
    private String cutRange;

    @DCCValue("scBlacklist:s02c02")
    private String scBlacklist;

    @DCCValue("cacheSwitch:0")
    private String cacheOpenSwitch;

    public boolean isDowngradeSwitch(){
        return "1".equals(downgradeSwitch);
    }

    public boolean isCutRange(String userId){
        //计算哈希码绝对值
        int hashCode = Math.abs(userId.hashCode());
        //获取后两位
        int lastTwoDigits = hashCode % 100;
        //判断是否在切量范围内
        return lastTwoDigits <= Integer.parseInt(cutRange);
    }

    /**
     * SC黑名单拦截
     * @param source
     * @param channel
     * @return
     */
    public boolean isSCBlackIntercept(String source, String channel){
        List<String> list = Arrays.asList(scBlacklist.split(Constants.SPLIT));
        return list.contains(source + channel);
    }

    public boolean isCacheOpenSwitch(){
        return "0".equals(cacheOpenSwitch);
    }

}
