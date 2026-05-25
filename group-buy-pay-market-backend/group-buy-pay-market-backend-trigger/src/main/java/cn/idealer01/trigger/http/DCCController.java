package cn.idealer01.trigger.http;

import cn.idealer.wrench.dynamic.config.center.domain.model.valobj.AttributeVO;
import cn.idealer01.api.IDCCService;
import cn.idealer01.api.response.Response;
import cn.idealer01.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/v1/gbm/dcc")
public class DCCController implements IDCCService {
    @Resource(name = "dynamicConfigCenterRedisTopic")
    private RTopic dccTopic;

    @GetMapping("update_config")
    @Override
    public Response<Boolean> updateCofig(String key, String value) {
        try{
            log.info("DCC 动态配置变更 key:{} value:{}", key, value);
            dccTopic.publish(new AttributeVO(key, value));
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        }catch (Exception e){
            log.error("DCC 动态配置变更失败 key:{} value:{}", key, value, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
