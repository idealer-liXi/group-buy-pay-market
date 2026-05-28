package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.CrowdTagsDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ICrowdTagsDetailDao {
    void addCrowdTagsUserId(CrowdTagsDetail crowdTagsDetail);

    List<CrowdTagsDetail> queryCrowdTagsDetailListByTagId(String tagId);

    List<CrowdTagsDetail> queryCrowdTagsDetailListByUserId(String userId);

    int countCrowdTagsDetailByTagId(String tagId);

    void deleteCrowdTagsUserId(@Param("tagId") String tagId, @Param("userId") String userId);
}
