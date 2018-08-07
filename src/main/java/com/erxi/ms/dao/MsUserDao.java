package com.erxi.ms.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.erxi.ms.domain.MsUser;

@Mapper
public interface MsUserDao {
	@Select("select * from ms_user where id=#{mobile}")
	public MsUser getById(@Param("mobile") Long mobile);

	@Update("update ms_user set password = #{password} where id = #{id}")
	public void update(MsUser toBeUpdate);
}
