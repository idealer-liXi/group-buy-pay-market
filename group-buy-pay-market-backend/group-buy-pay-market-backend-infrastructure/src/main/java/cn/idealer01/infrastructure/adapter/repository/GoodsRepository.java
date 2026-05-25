package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.goods.adapt.repository.IGoodsRepository;
import cn.idealer01.infrastructure.dao.IOrderDao;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Repository
public class GoodsRepository implements IGoodsRepository {
    @Resource
    private IOrderDao orderDao;
    @Override
    public void changeOrderDealDone(String tradeNo) {
        orderDao.changeOrderDealDone(tradeNo);
    }
}
