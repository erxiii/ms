package com.erxi.ms.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;

import com.erxi.ms.domain.MsOrder;
import com.erxi.ms.domain.OrderInfo;

@Mapper
public interface OrderDao {
	@Select("select * from ms_order where user_id=#{userId} and goods_id=#{goodsId}")
	MsOrder getMiaoshaOrderByUserIdGoodsId(@Param("userId") Long userId,
			@Param("goodsId") Long goodsId);

	@Insert("insert into order_info(user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date)values("
			+ "#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel},#{status},#{createDate} )")
	@SelectKey(keyColumn="id", keyProperty="id", resultType=long.class, before=false, statement="select last_insert_id()")
	long insert(OrderInfo orderInfo);

	@Insert("insert into ms_order (user_id, goods_id, order_id)values(#{userId}, #{goodsId}, #{orderId})")
	void insertMiaoshaOrder(MsOrder msOrder);

	@Select("select * from order_info where id = #{orderId}")
	public OrderInfo getOrderById(@Param("orderId")long orderId);
	
	@Delete("delete from order_info")
	public void deleteOrders();

	@Delete("delete from ms_order")
	public void deleteMiaoshaOrders();
}
