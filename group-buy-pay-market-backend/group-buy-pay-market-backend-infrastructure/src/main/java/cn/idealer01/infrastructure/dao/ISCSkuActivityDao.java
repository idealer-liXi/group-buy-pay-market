package cn.idealer01.infrastructure.dao;

import cn.idealer01.domain.activity.model.valobj.SCSkuActivityVO;
import cn.idealer01.infrastructure.dao.po.SCSkuActivity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ISCSkuActivityDao {
    SCSkuActivity querySCSkuActivityBySCGoodsId(SCSkuActivity scSkuActivity);
}
