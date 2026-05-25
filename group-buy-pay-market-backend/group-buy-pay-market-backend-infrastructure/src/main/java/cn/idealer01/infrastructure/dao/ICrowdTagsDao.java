package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.CrowdTags;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ICrowdTagsDao {
    void updateCrowdTagsStatistics(CrowdTags crowdTagsReq);
}
