package com.spring.quartz.job;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.spring.quartz.entity.ScheduleJob;
import com.spring.quartz.service.IrpTaskService;

@Controller
public class LoadTask {
	public LoadTask(){}
	private final Logger logger = Logger.getLogger(this.getClass());  
    @Autowired
	private IrpTaskService irpTaskService;    
    /**
     * 初始化数据库中定时任务
     * @throws SchedulerException
     */
	public void initTask() throws SchedulerException {		     
		// 可执行的任务列表        
		List<ScheduleJob> taskList = irpTaskService.getTaskList();  
		logger.info("初始化加载定时任务......"); 
		for (ScheduleJob job : taskList) {     
			irpTaskService.addJob(job);     
		}     
	}	     
}
