package com.ict.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DAO {
	// HomeController 에서 가져옴;;
	private static final Logger logger = LoggerFactory.getLogger(DAO.class);
	
	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;
	
	public List<VO> getGuestBook2List() {
		try {
			// List<VO> list = sqlSessionTemplate.selectList("");
			// return list;
			return sqlSessionTemplate.selectList("guestbook2.list");
		} catch (Exception e) {
			System.out.println(e);
			logger.info("list", e);
		}
		return null;
	}
	
	public int getGuestBook2Insert(VO vo) {
		try {
			return sqlSessionTemplate.insert("guestbook2.insert", vo);
		} catch (Exception e) {
			System.out.println(e);
		}
		return -1;
	}
	
	public VO getGuestBook2Detail(String idx) {
		try {
			return sqlSessionTemplate.selectOne("guestbook2.detail", idx); 
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}
	
	public int getGuestBook2Delete(String idx) {
		try {
			return sqlSessionTemplate.delete("guestbook2.delete", idx); 
		} catch (Exception e) {
			System.out.println(e);
		}
		return -1;
	}
	
	public int getGuestBook2Update(VO vo) {
		try {
			return sqlSessionTemplate.delete("guestbook2.update", vo); 
		} catch (Exception e) {
			System.out.println(e);
		}
		return -1;
	}
	
}
