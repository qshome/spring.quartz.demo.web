package com.spring.quartz.job;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

import com.spring.quartz.entity.ScheduleJob;
import com.spring.quartz.service.IrpTaskService;
import com.spring.quartz.util.SpringUtil;


/**     
 * Job有状态实现类，不允许并发执行     
 * 	若一个方法一次执行不完下次轮转时则等待该方法执行完后才执行下一次操作--该限制是针对JobDetail(Job实例)的，而不是job类的    
 * 	主要是通过注解：@DisallowConcurrentExecution         
 *     
 */     
//@PersistJobDataAfterExecution：将该注解加在job类上，告诉Quartz在成功执行了job类的execute方法后（没有发生任何异常），更新JobDetail中JobDataMap的数据
//需要与@DisallowConcurrentExecution注解同时使用，因为当同一个job（JobDetail）的两个实例被并发执行时，由于竞争，JobDataMap会存在线程安全问题
@DisallowConcurrentExecution 
public class QuartzJobFactoryDisallowConcurrentExecution implements Job {     
    
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