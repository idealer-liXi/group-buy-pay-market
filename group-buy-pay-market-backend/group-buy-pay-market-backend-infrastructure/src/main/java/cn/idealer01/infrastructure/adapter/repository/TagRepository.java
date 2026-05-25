package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.tag.adapter.resposity.ITagRepository;
import cn.idealer01.domain.tag.model.CrowdTagsJobEntity;
import cn.idealer01.infrastructure.dao.ICrowdTagsDao;
import cn.idealer01.infrastructure.dao.ICrowdTagsDetailDao;
import cn.idealer01.infrastructure.dao.ICrowdTagsJobDao;
import cn.idealer01.infrastructure.dao.po.CrowdTags;
import cn.idealer01.infrastructure.dao.po.CrowdTagsDetail;
import cn.idealer01.infrastructure.dao.po.CrowdTagsJob;
import cn.idealer01.infrastructure.redis.IRedisService;
import org.redisson.api.RBitSet;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TagRepository implements ITagRepository {
    @Resource
    private ICrowdTagsDao crowdTagsDao;

    @Resource
    private ICrowdTagsJobDao crowdTagsJobDao;

    @Resource
    private ICrowdTagsDetailDao crowdTagsDetailDao;

    @Resource
    private IRedisService redisService;

    @Override
    public CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId) {
        CrowdTagsJob crowdTagsJobReq = new CrowdTagsJob();
        crowdTagsJobReq.setTagId(tagId);
        crowdTagsJobReq.setBatchId(batchId);

        CrowdTagsJob crowdTagsJobRes = crowdTagsJobDao.queryCrowdTagsJob(crowdTagsJobReq);
        if (null == crowdTagsJobRes) return null;

        return CrowdTagsJobEntity.builder()
                .tagType(crowdTagsJobRes.getTagType())
                .tagRule(crowdTagsJobRes.getTagRule())
                .statEndTime(crowdTagsJobRes.getStatEndTime())
                .statEndTime(crowdTagsJobRes.getStatEndTime())
                .build();
    }

    @Override
    public void addCrowdTagsJobEntity(String tagId, String userId) {
        CrowdTagsDetail crowdTagsDetailReq = new CrowdTagsDetail();
        crowdTagsDetailReq.setTagId(tagId);
        crowdTagsDetailReq.setUserId(userId);

        try{
            crowdTagsDetailDao.addCrowdTagsUserId(crowdTagsDetailReq);

            //获取BitSet
            RBitSet bitSet = redisService.getBitSet(tagId);
            bitSet.set(redisService.getIndexFromUserId(userId), true);
        } catch (DuplicateKeyException ignore){
            //忽略唯一索引冲突
        }

    }

    @Override
    public void updateCrowdTagsJobEntity(String tagId, int count) {
        CrowdTags crowdTagsReq = new CrowdTags();
        crowdTagsReq.setTagId(tagId);
        crowdTagsReq.setStatistics(count);
        crowdTagsDao.updateCrowdTagsStatistics(crowdTagsReq);
    }
}
