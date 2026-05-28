package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.CrowdTags;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ICrowdTagsDao {
    CrowdTags queryCrowdTagsByTagId(String tagId);

    List<CrowdTags> queryCrowdTagsList();

    void insertCrowdTags(CrowdTags crowdTags);

    void updateCrowdTags(CrowdTags crowdTags);

    void updateCrowdTagsStatistics(CrowdTags crowdTagsReq);

    void updateCrowdTagsStatisticsTo(@Param("tagId") String tagId, @Param("statistics") int statistics);
}
