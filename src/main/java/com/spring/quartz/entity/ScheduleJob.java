package com.spring.quartz.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
@Entity
@Table(name = "b_job")
public class ScheduleJob implements Serializable{

	private static final long serialVersionUID = 5624244762723335843L;
	public static final String STATUS_RUNNING = "1";     
	public static final String STATUS_NOT_RUNNING = "0";     
	public static final String CONCURRENT_IS = "1";     
	public static final String CONCURRENT_NOT = "0";     
	
	public static final String RUNSTATUS_INIT = "0";     
	public static final String RUNSTATUS_RUNNING = "1";     
	public static final String RUNSTATUS_COMPLETE = "2";  
	     
	@Id
	@GeneratedValue(strategy =GenerationType.IDENTITY)
	@Column(name = "id", length = 32)
	/** 任务id */     
    private String jobId;  
	
	@Column
    /** 任务名称 */     
    private String jobName;  
	
	@Column
	/** 任务分组，任务名称+组名称应该是唯一的 */     
    private String jobGroup;    
	
	@Column
    /** 任务初始状态 0禁用 1启用 2删除*/     
    private String jobStatus; 
	
	@Column
    /** 任务运行状态 0未运行 1运行中 2运行完成*/     
    private String runStatus; 
	
	@Column
    /** 任务是否有状态（允许并发） */     
	private String isConcurrent = "1";  
	
	@Column   
    /** 任务运行时间表达式 */     
    private String cronExpression;  
	
	@Column
    /** 任务描述 */     
    private String description;  
	
	@Column
    /** 任务调用类在spring中注册的bean id，如果spingId不为空，则按springId查找 */     
    private String springId;    
	
	@Column   
    /** 任务调用类名，包名+类名，通过类反射调用 ，如果spingId为空，则按jobClass查找   */     
    private String jobClass;
	
	@Column    
    /** 任务调用的方法名 */     
	private String methodName;
	
	@Temporal(TemporalType.TIMESTAMP)    
    /** 时钟触发器启动时间 */     
    private Date startTime; 
	
	@Temporal(TemporalType.TIMESTAMP)    
    /** 时钟触发器结束时间 */     
    private Date endTime;  
	
	@Temporal(TemporalType.TIMESTAMP)     
    /** 前一次运行时间 */     
    private Date previousTime; 
	
	@Temporal(TemporalType.TIMESTAMP)     
    /** 下次运行时间 */     
    private Date nextTime;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobGroup() {
		return jobGroup;
	}

	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}

	public String getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}

	public String getRunStatus() {
		return runStatus;
	}

	public void setRunStatus(String runStatus) {
		this.runStatus = runStatus;
	}

	public String getIsConcurrent() {
		return isConcurrent;
	}

	public void setIsConcurrent(String isConcurrent) {
		this.isConcurrent = isConcurrent;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSpringId() {
		return springId;
	}

	public void setSpringId(String springId) {
		this.springId = springId;
	}

	public String getJobClass() {
		return jobClass;
	}

	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getPreviousTime() {
		return previousTime;
	}

	public void setPreviousTime(Date previousTime) {
		this.previousTime = previousTime;
	}

	public Date getNextTime() {
		return nextTime;
	}

	public void setNextTime(Date nextTime) {
		this.nextTime = nextTime;
	}
}
