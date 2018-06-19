package com.spring.quartz.dao;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTemplate;

public class BaseDao{
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	public void save(Serializable entity) {
		hibernateTemplate.save(entity);
	}
	
	public void update(Serializable entity) {
		hibernateTemplate.update(entity);
	}
	
	public void delete(Serializable entity) {
		hibernateTemplate.delete(entity);
	}
	
	public List<?> find(String queryString,Object...values ) {
		return hibernateTemplate.find(queryString, values);
	}
}
