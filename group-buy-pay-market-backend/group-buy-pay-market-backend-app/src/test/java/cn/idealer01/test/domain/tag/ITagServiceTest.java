package cn.idealer01.test.domain.tag;

import cn.idealer01.domain.tag.service.ITagService;
import cn.idealer01.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBitSet;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class ITagServiceTest {
    @Resource
    private ITagService tagService;
    @Resource
    private IRedisService redisService;

    @Test
    public void test_tag_job(){
        tagService.execTagBatchJob("RQ_KJHKL98UU78H66554GFDV", "10001");
    }

    @Test
    public void test_bitset_map(){
        RBitSet bitset = redisService.getBitSet("RQ_KJHKL98UU78H66554GFDV");

        log.info("xiaofuge存在，预期为true，测试结果：{}", bitset.get(redisService.getIndexFromUserId("xiaofuge")));
        log.info("xiaoli不存在，预期为false，测试结果：{}", bitset.get(redisService.getIndexFromUserId("xiaoli")));
    }


}
