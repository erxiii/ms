package com.erxi.ms.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.erxi.ms.domain.MsGoods;
import com.erxi.ms.vo.GoodsVo;

@Mapper
public interface GoodsDao {
	@Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.ms_price from ms_goods mg left join goods g on mg.goods_id = g.id")
	public List<GoodsVo> listGoodsVo();

	@Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.ms_price from ms_goods mg left join goods g on mg.goods_id = g.id where g.id = #{goodsId}")
	public GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

	@Update("update ms_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0")
	public int reduceStock(MsGoods g);

	@Update("update ms_goods set stock_count = #{stockCount} where goods_id = #{goodsId}")
	public int resetStock(MsGoods g);
}
