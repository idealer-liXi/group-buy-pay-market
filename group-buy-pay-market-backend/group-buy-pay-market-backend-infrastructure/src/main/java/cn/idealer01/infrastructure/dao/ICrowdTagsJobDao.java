package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.CrowdTagsJob;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ICrowdTagsJobDao {
    CrowdTagsJob queryCrowdTagsJob(CrowdTagsJob crowdTagsJobReq);
}
