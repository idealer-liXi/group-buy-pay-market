package cn.idealer01.domain.tag.service;

import cn.idealer01.domain.tag.adapter.resposity.ITagRepository;
import cn.idealer01.domain.tag.model.CrowdTagsJobEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TagService implements ITagService{
    @Resource
    private ITagRepository repository;

    @Override
    public void execTagBatchJob(String tagId, String batchId) {
        log.info("人群标签批次任务 tagId:{} batchId:{}", tagId, batchId);
        //1.查询批次任务
        CrowdTagsJobEntity crowdTagsJobEntity = repository.queryCrowdTagsJobEntity(tagId, batchId);

        //2.采集用户信息

        //3.数据写入记录
        List<String> userIdList = new ArrayList<String>(){
            {
                add("xiaofuge");
                add("liergou");
            }
        };

        //4.公司中有专门的数据库团队编写脚本写入数据库
        for (String userId : userIdList) {
            repository.addCrowdTagsJobEntity(tagId, userId);
        }

        //5.更新人群标签统计量
        repository.updateCrowdTagsJobEntity(tagId, userIdList.size());
    }
}
