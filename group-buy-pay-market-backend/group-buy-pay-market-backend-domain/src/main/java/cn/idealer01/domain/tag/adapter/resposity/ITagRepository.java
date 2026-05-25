package cn.idealer01.domain.tag.adapter.resposity;

import cn.idealer01.domain.tag.model.CrowdTagsJobEntity;

public interface ITagRepository {

    CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId);

    void addCrowdTagsJobEntity(String tagId, String userId);

    void updateCrowdTagsJobEntity(String tagId, int count);
}
