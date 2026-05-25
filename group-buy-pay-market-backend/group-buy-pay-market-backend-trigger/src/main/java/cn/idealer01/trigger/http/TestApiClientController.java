package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.NotifyTaskRequestDTO;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@Slf4j
@RequestMapping("/api/v1/test/")
public class TestApiClientController {

    @PostMapping("group_buy_notify")
    public String groupBuyNotify(@RequestBody NotifyTaskRequestDTO notifyTaskRequestDTO){
        log.info("模拟第三方服务接收拼团回调 {}", JSON.toJSONString(notifyTaskRequestDTO));
        return "success";
    }

}
