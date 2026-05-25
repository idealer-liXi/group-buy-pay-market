package cn.idealer01.domain.goods.service;

import cn.idealer01.domain.goods.adapt.repository.IGoodsRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GoodsService implements IGoodsService{
    @Resource
    private IGoodsRepository repository;

    @Override
    public void changeOrderDealDone(String tradeNo) {
        repository.changeOrderDealDone(tradeNo);
    }
}
