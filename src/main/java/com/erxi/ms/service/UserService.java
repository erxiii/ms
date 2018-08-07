package com.erxi.ms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erxi.ms.dao.UserDao;
import com.erxi.ms.domain.User;

@Service
public class UserService {
	@Autowired
	UserDao userDao;

	public User getById(int id) {
		return userDao.getById(id);
	}

	//事务回滚不执行
//	@Transactional
	public boolean tx() {
		User u1=new User();
		u1.setId(2);
		u1.setName("222");
		userDao.insert(u1);
		
		User u2=new User();
		u2.setId(1);
		u2.setName("111");
		userDao.insert(u2);
		
		return true;
	}
}
