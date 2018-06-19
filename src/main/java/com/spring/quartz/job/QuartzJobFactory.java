package com.spring.quartz.job;


import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

import com.spring.quartz.entity.ScheduleJob;
import com.spring.quartz.service.IrpTaskService;
import com.spring.quartz.util.SpringUtil;


/**     
 * Job实现类  无状态     
 * 若此方法上一次还未执行完，而下一次执行时间轮到时则该方法也可并发执行      
 */    
public class QuartzJobFactory implements Job {

	private final Logger logger = Logger.getLogger(this.getClass());     

	@Override     
    public void execute(JobExecutionContext context) throws JobExecutionException {
		ScheduleJob scheduleJob = (ScheduleJob)context.getMergedJobDataMap().get("scheduleJob");
		//获取触发器信息
		Trigger trigger = context.getTrigger();
		scheduleJob.setPreviousTime(trigger.getPreviousFireTime());
		scheduleJob.setNextTime(trigger.getNextFireTime());
		scheduleJob.setStartTime(trigger.getStartTime());
		//获取服务类
		IrpTaskService irpTaskService = SpringUtil.getBean("irpTaskService");
        logger.info("任务名称 = [" + scheduleJob.getJobGroup()+"-"+scheduleJob.getJobName() + "]运行中...");  
        try {
        	//执行任务
        	irpTaskService.invokMethod(scheduleJob);  
		} catch (Exception e) {
		    logger.info("运行失败，任务名称 = [" + scheduleJob.getJobGroup()+"-"+scheduleJob.getJobName() + "]");  
		    logger.error("定时任务删除失败！具体异常信息如下：\n"+irpTaskService.handleException(e));
			return;
		}
        logger.info("任务名称 = [" + scheduleJob.getJobGroup()+"-"+scheduleJob.getJobName() + "]执行完毕");  
    }     
	     
}
