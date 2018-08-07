package com.erxi.ms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erxi.ms.dao.GoodsDao;
import com.erxi.ms.domain.MsGoods;
import com.erxi.ms.vo.GoodsVo;

@Service
public class GoodsService {

	@Autowired
	GoodsDao goodsDao;

	public List<GoodsVo> listGoodsVo() {
		return goodsDao.listGoodsVo();
	}

	public GoodsVo getGoodsVoByGoodsId(long goodsId) {
		return goodsDao.getGoodsVoByGoodsId(goodsId);
	}

	public boolean reduceStock(GoodsVo goodsVo) {
		MsGoods goods = new MsGoods();
		goods.setGoodsId(goodsVo.getId());
		int ret = goodsDao.reduceStock(goods);
		return ret > 0;
	}
	
	public void resetStock(List<GoodsVo> goodsList) {
		for(GoodsVo goods : goodsList ) {
			MsGoods g = new MsGoods();
			g.setGoodsId(goods.getId());
			g.setStockCount(goods.getStockCount());
			goodsDao.resetStock(g);
		}
	}
}
