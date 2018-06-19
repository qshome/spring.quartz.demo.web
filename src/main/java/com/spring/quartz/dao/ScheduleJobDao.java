package com.spring.quartz.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.spring.quartz.entity.ScheduleJob;

@Repository
public class ScheduleJobDao extends BaseDao{
	//@Autowired
	//public HibernateTemplate hibernateTemplate;
	
	@SuppressWarnings("unchecked")
	public List<ScheduleJob> getTaskList(String hql,Object[] objArray){
		//return (List<ScheduleJob>) this.hibernateTemplate.find(hql,objArray);
		return (List<ScheduleJob>) this.find(hql,objArray);
	}
}
