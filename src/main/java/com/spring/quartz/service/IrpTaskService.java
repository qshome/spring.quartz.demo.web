package com.spring.quartz.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.spring.quartz.annotation.MyLog;
import com.spring.quartz.dao.ScheduleJobDao;
import com.spring.quartz.entity.ScheduleJob;
import com.spring.quartz.job.QuartzJobFactory;
import com.spring.quartz.job.QuartzJobFactoryDisallowConcurrentExecution;
import com.spring.quartz.util.SpringUtil;

@Service("irpTaskService")
public class IrpTaskService {
	private final Logger logger = Logger.getLogger(IrpTaskService.class);
	@Autowired     
	private SchedulerFactoryBean schedulerFactoryBean;
	@Autowired    
	private ScheduleJobDao scheduleJobDao;
	
	
	public boolean saveScheduleJob(ScheduleJob job) throws SchedulerException {
		scheduleJobDao.save(job);
		return addJob(job);
	}
	
	/**    
	 * 添加任务    
	 *     
	 * @param scheduleJob    
	 * @throws SchedulerException    
	 */    
	@MyLog
	public boolean addJob(ScheduleJob job) throws SchedulerException {    
		if (job == null || !ScheduleJob.STATUS_RUNNING.equals(job.getJobStatus())) {    
			return false;    
		}    
		//判断表达式是否有效
		if(!CronExpression.isValidExpression(job.getCronExpression())){
			logger.error("时间表达式错误（"+job.getJobGroup()+","+job.getJobName()+"）,"+job.getCronExpression());  
			return false;    
		}else{    
			Scheduler scheduler = schedulerFactoryBean.getScheduler();    
			// 任务名称和任务组设置规则：    // 名称：task_1 ..    // 组 ：group_1 ..        
			TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());    
			CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);        
			// 不存在，创建一个       
			if (null == trigger) {    
				//是否允许并发执行    
				Class<? extends Job> clazz = ScheduleJob.CONCURRENT_IS.equals(job.getIsConcurrent()) ? QuartzJobFactory.class : QuartzJobFactoryDisallowConcurrentExecution.class;    
				JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(job.getJobName(), job.getJobGroup()).build();     
				jobDetail.getJobDataMap().put("scheduleJob", job);     
				// 表达式调度构建器         
				//CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
				//不触发立即执行,等待下次Cron触发频率到达时刻开始按照Cron频率依次执行
				CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression()).withMisfireHandlingInstructionDoNothing();

				// 按新的表达式构建一个新的trigger 
				if(job.getStartTime()!=null&&job.getEndTime()!=null){
					trigger = TriggerBuilder.newTrigger().startAt(job.getStartTime()).endAt(job.getEndTime()).withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
				}else if(job.getStartTime()!=null){
					trigger = TriggerBuilder.newTrigger().startAt(job.getStartTime()).withIdentity(triggerKey).withSchedule(scheduleBuilder).build(); 
				}else if(job.getEndTime()!=null){
					trigger = TriggerBuilder.newTrigger().endAt(job.getEndTime()).withIdentity(triggerKey).withSchedule(scheduleBuilder).build(); 
				}else{
					trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).build(); 
				}
				// 按新的表达式构建一个新的trigger 
				scheduler.scheduleJob(jobDetail, trigger); 
			} else {     // trigger已存在，则更新相应的定时设置        
				//CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());  
				CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression()).withMisfireHandlingInstructionDoNothing();
				// 按新的cronExpression表达式重新构建trigger         
				trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();        
				// 按新的trigger重新设置job执行         
				scheduler.rescheduleJob(triggerKey, trigger);       
			}	
			if(!scheduler.isShutdown()) {
				logger.info("定时任务启动中...");
				scheduler.start();
				logger.info("定时任务启动成功");
			}
		} 
		logger.info("任务"+job.getJobGroup()+"-"+job.getJobName()+"添加成功！");
		return true;    
	}     
	
	/**    
	 * 暂停任务 --任务恢复时可能补充执行中途丢失的job   
	 * @param scheduleJob    
	 * @return    
	 */    
	public boolean pauseJob(ScheduleJob scheduleJob){    
		Scheduler scheduler = schedulerFactoryBean.getScheduler();    
		JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());    
		try {    
			scheduler.pauseJob(jobKey);    
			return true;    
		} catch (SchedulerException e) {			    
		}    
		return false;    
	} 
	
	/**    
	 * 恢复任务--任务会立即执行一次且下一次任务按最近一次时钟表达式约定的时间执行    
	 * @param scheduleJob    
	 * @return    
	 */    
	public boolean resumeJob(ScheduleJob scheduleJob){    
		Scheduler scheduler = schedulerFactoryBean.getScheduler();    
		JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());    
		try {    
			scheduler.resumeJob(jobKey);    
			return true;    
		} catch (SchedulerException e) {			    
		}    
		return false;    
	}
	
	/**    
	 * 删除任务    
	 */    
	public boolean deleteJob(ScheduleJob scheduleJob){    
		Scheduler scheduler = schedulerFactoryBean.getScheduler();    
		JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());    
		//取当前任务数据库实体
		List<ScheduleJob> jobList = getDataBaseJob(scheduleJob.getJobName(),scheduleJob.getJobGroup());
		try{    
			scheduler.deleteJob(jobKey); 
			//更新数据库job状态
			if(!jobList.isEmpty()) {
				ScheduleJob job =jobList.get(0);
				job.setJobStatus("2");
				//scheduleJobDao.hibernateTemplate.update(job);
				scheduleJobDao.update(job);
			}
			logger.info("任务"+scheduleJob.getJobGroup()+"-"+scheduleJob.getJobName()+"删除成功！");
			System.out.println("任务"+scheduleJob.getJobGroup()+"-"+scheduleJob.getJobName()+"删除成功！");
			return true;    
		} catch (Exception e) {		
			logger.info("任务"+scheduleJob.getJobGroup()+"-"+scheduleJob.getJobName()+"删除失败！");
//			//抓取异常栈信息
//			ByteArrayOutputStream os = new ByteArrayOutputStream();  
//			e.printStackTrace(new PrintStream(os));  
//			String exception = os.toString();  
			logger.error("定时任务删除失败！具体异常信息如下：\n"+handleException(e));
		}    
		return false;    
	}
	
	/**    
	 * 立即执行一个任务    
	 * @param scheduleJob    
	 * @throws SchedulerException    
	 */    
	public void executeJobNow(ScheduleJob scheduleJob) throws SchedulerException{    
		Scheduler scheduler = schedulerFactoryBean.getScheduler();    
		JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());    
		scheduler.triggerJob(jobKey);    
	} 
	
	/**    
	 * 更新任务时间表达式    
	 * @param scheduleJob    
	 * @throws SchedulerException    
	 */    
	public void updateCronExpression(ScheduleJob scheduleJob) throws SchedulerException{    
		Scheduler scheduler = schedulerFactoryBean.getScheduler();    
		TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());    
		//获取trigger，即在spring配置文件中定义的 bean id="myTrigger"    
		CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);    
		//表达式调度构建器    
		//CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression());  
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression()).withMisfireHandlingInstructionDoNothing();
		//按新的cronExpression表达式重新构建trigger    
		trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();    
		//按新的trigger重新设置job执行    
		scheduler.rescheduleJob(triggerKey, trigger);  
		//取当前任务数据库实体
		List<ScheduleJob> jobList = getDataBaseJob(scheduleJob.getJobName(),scheduleJob.getJobGroup());
		//更新数据库job时钟表达式
		if(!jobList.isEmpty()) {
			ScheduleJob job =jobList.get(0);
			job.setCronExpression(scheduleJob.getCronExpression());;
			//scheduleJobDao.hibernateTemplate.update(job);
			scheduleJobDao.update(job);
		}
	}
	
	/**    
	 * 获取单个任务    
	 * @param jobName    
	 * @param jobGroup    
	 * @return    
	 * @throws SchedulerException    
	 */    
	public ScheduleJob getJob(String jobName,String jobGroup) throws SchedulerException{    
		ScheduleJob job = null;    
		Scheduler scheduler = schedulerFactoryBean.getScheduler();    
		TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);    
		CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);      
		if (null != trigger) {    
			job = new ScheduleJob();    
	        job.setJobName(jobName);    
	        job.setJobGroup(jobGroup);    
	        job.setDescription("触发器:" + trigger.getKey());    
	        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());    
	        job.setJobStatus(triggerState.name());    
	        if (trigger instanceof CronTrigger) {    
	            CronTrigger cronTrigger = (CronTrigger) trigger;    
	            String cronExpression = cronTrigger.getCronExpression();    
	            job.setCronExpression(cronExpression);    
	        }    
		}		    
        return job;    
	}
	
	
	/**    
	 * 获取所有任务    
	 * @return    
	 * @throws SchedulerException    
	 */    
	public List<ScheduleJob> getAllJobs() throws SchedulerException{    
		Scheduler scheduler = schedulerFactoryBean.getScheduler();      
		GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();    
		Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);    
		List<ScheduleJob> jobList = new ArrayList<ScheduleJob>();    
		for (JobKey jobKey : jobKeys) {    
		    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);    
		    for (Trigger trigger : triggers) {    
		    	ScheduleJob job = new ScheduleJob();    
		        job.setJobName(jobKey.getName());    
		        job.setJobGroup(jobKey.getGroup());    
		        job.setDescription("触发器:" + trigger.getKey());   
		        //触发器当前状态    
		        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());    
		        job.setJobStatus(triggerState.name());    
		        if (trigger instanceof CronTrigger) {    
		            CronTrigger cronTrigger = (CronTrigger) trigger;    
		            String cronExpression = cronTrigger.getCronExpression();   
		            job.setCronExpression(cronExpression);    
		        }    
		        jobList.add(job);    
		    }    
		}    
		return jobList;    
	} 
	
	/**    
	 * 所有正在运行的job    
	 * @return    
	 * @throws SchedulerException    
	 */    
	public List<ScheduleJob> getRunningJob() throws SchedulerException {    
		Scheduler scheduler = schedulerFactoryBean.getScheduler();    
		List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();    
		List<ScheduleJob> jobList = new ArrayList<ScheduleJob>(executingJobs.size());    
		for (JobExecutionContext executingJob : executingJobs) {    
			ScheduleJob job = new ScheduleJob();    
			JobDetail jobDetail = executingJob.getJobDetail();    
			JobKey jobKey = jobDetail.getKey();    
			Trigger trigger = executingJob.getTrigger();    
			job.setJobName(jobKey.getName());    
			job.setJobGroup(jobKey.getGroup());    
			job.setDescription("触发器:" + trigger.getKey());    
			Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());    
			job.setJobStatus(triggerState.name());    
			if (trigger instanceof CronTrigger) {    
				CronTrigger cronTrigger = (CronTrigger) trigger;    
				String cronExpression = cronTrigger.getCronExpression();    
				job.setCronExpression(cronExpression);    
			}    
			jobList.add(job);    
		}    
		return jobList;    
	}   
	
	/**
	 * 判断任务是否正在运行中
	 * @param IrpJobEntity
	 * @return
	 * @throws SchedulerException
	 */
	public boolean isRunning(ScheduleJob job) throws SchedulerException{
		List<ScheduleJob> list = getRunningJob();
		for(ScheduleJob irpJobEntity : list){
			if(irpJobEntity.getJobName().equals(job.getJobName())&&irpJobEntity.getJobGroup().equals(job.getJobGroup())){
				return true;
			}
		}
		return false;
	}
	
	/**     
	 * 通过反射调用scheduleJob中定义的方法     
	 *      
	 * @param scheduleJob     
	 * @throws Exception 
	 */     
	public void invokMethod(ScheduleJob scheduleJob) throws Exception {     
		Object object = null;     
		Class<?> clazz = null;     
        //springId不为空先按springId查找bean     
		if (StringUtils.isNotBlank(scheduleJob.getSpringId())) {     
			object = SpringUtil.getBean(scheduleJob.getSpringId());     
		} else if (StringUtils.isNotBlank(scheduleJob.getJobClass())) {//按jobClass查找     
			try {     
				clazz = Class.forName(scheduleJob.getJobClass());     
				object = clazz.newInstance();     
			} catch (Exception e) {     
				e.printStackTrace();   
				throw e;
			}     
		}     
		if (object == null) {     
			logger.error("任务名称 = [" + scheduleJob.getJobName() + "]---------------未启动成功，请检查执行类是否配置正确！！！"); 
			return;     
		}     
		clazz = object.getClass();     
		Method method = null;     
		try {     
			method = clazz.getDeclaredMethod(scheduleJob.getMethodName());     
			//method = clazz.getDeclaredMethod(scheduleJob.getMethodName(),String.class);     
		} catch (NoSuchMethodException e) {     
			logger.error("任务名称 = [" + scheduleJob.getJobName() + "]---------------未启动成功，请检查执行类的方法名是否设置错误！！！");    
			throw e;
		} catch (SecurityException e) {   
			logger.error("任务名称 = [" + scheduleJob.getJobName() + "]---------------未启动成功，请检查执行类的方法名是否设置错误！！！");
			e.printStackTrace();	
			throw e;
		}     
		if (method != null) {     
			try {     
				method.invoke(object);     
			} catch (IllegalAccessException e) {     
				e.printStackTrace();     
				throw e;
			} catch (IllegalArgumentException e) {     
				e.printStackTrace();     
				throw e;
			} catch (InvocationTargetException e) {     
				e.printStackTrace();     
				throw e;
			}     
		}		     
	} 
	
	/**    
	 * 从数据库查询任务列表    
	 * @return    
	 */    
	public List<ScheduleJob> getDataBaseJob(String jobName,String jobGroup){  
		String hql = "from ScheduleJob t where t.jobName = ? and t.jobGroup = ?";
		@SuppressWarnings("unchecked")
		//List<ScheduleJob> jobs = (List<ScheduleJob>) scheduleJobDao.hibernateTemplate.find(hql, new Object[]{jobName,jobGroup});
		List<ScheduleJob> jobs = (List<ScheduleJob>) scheduleJobDao.find(hql, jobName,jobGroup);
		return jobs;    
	} 
	
	public List<ScheduleJob> getTaskList(){
		String hql="from ScheduleJob where jobStatus = ? ";
		@SuppressWarnings("unchecked")
		//List<ScheduleJob> jobs = (List<ScheduleJob>) scheduleJobDao.hibernateTemplate.find(hql, "1");
		List<ScheduleJob> jobs = (List<ScheduleJob>) scheduleJobDao.find(hql, "1");
		return jobs;
	}
	
	//处理异常
	public String handleException(Exception e){
		ByteArrayOutputStream os = new ByteArrayOutputStream();  
		//抓取异常栈信息
		try {
			e.printStackTrace(new PrintStream(os));  
			String exception = os.toString();  
			//if(exception.length()>512){//数据库字段长度
			//	exception = exception.substring(0,511);
			//}
			return exception;
		}finally {
			try {
				os.flush();
				os.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
