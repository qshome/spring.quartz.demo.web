package com.spring.quartz.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.quartz.entity.ScheduleJob;
import com.spring.quartz.service.IrpTaskService;

@Controller
public class ScheduleJobController {
	@Autowired
	private IrpTaskService irpTaskService;
	
	@RequestMapping("query.do")
	@ResponseBody
	public List<ScheduleJob> DataSourceQuery() throws SchedulerException {
		return irpTaskService.getAllJobs();
	}
	
	@RequestMapping("getJobByJobNameAndJobGroup.do")
	@ResponseBody
	public ScheduleJob getJobByJobNameAndJobGroup(HttpServletRequest request,String jobName,String jobGroup) {
		List<ScheduleJob> list = irpTaskService.getDataBaseJob(jobName,jobGroup);
		if(list!=null&&!list.isEmpty()) {
			return list.get(0);	
		}
		return null;
	}
	
	@RequestMapping("pauserJob.do")
	@ResponseBody
	public String pauserJob(HttpServletRequest request,String jobName,String jobGroup) {
		ScheduleJob scheduleJob=new ScheduleJob();
		scheduleJob.setJobName(jobName);
		scheduleJob.setJobGroup(jobGroup);
		if(irpTaskService.pauseJob(scheduleJob)) {
			return "sucess";
		}
		return "error";
	}
	
	@RequestMapping("resumeJob.do")
	@ResponseBody
	public String resumeJob(HttpServletRequest request,String jobName,String jobGroup) {
		ScheduleJob scheduleJob=new ScheduleJob();
		scheduleJob.setJobName(jobName);
		scheduleJob.setJobGroup(jobGroup);
		if(irpTaskService.resumeJob(scheduleJob)) {
			return "sucess";
		}
		return "error";
	}
	
	@RequestMapping("executeJobNow.do")
	@ResponseBody
	public String executeJobNow(HttpServletRequest request,String jobName,String jobGroup) {
		ScheduleJob scheduleJob=new ScheduleJob();
		scheduleJob.setJobName(jobName);
		scheduleJob.setJobGroup(jobGroup);
		try {
			irpTaskService.executeJobNow(scheduleJob);
			return "sucess";
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return "error";
	}
	
	@RequestMapping("updateCronExpression.do")
	@ResponseBody
	public String updateCronExpression(HttpServletRequest request,String jobName,String jobGroup,String cronExpression) {
		ScheduleJob scheduleJob=new ScheduleJob();
		scheduleJob.setJobName(jobName);
		scheduleJob.setJobGroup(jobGroup);
		scheduleJob.setCronExpression(cronExpression);
		try {
			irpTaskService.updateCronExpression(scheduleJob);
			return "sucess";
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return "error";
	}
	
	@RequestMapping("deleteJob.do")
	@ResponseBody
	public String deleteJob(HttpServletRequest request,String jobName,String jobGroup) {
		ScheduleJob scheduleJob=new ScheduleJob();
		scheduleJob.setJobName(jobName);
		scheduleJob.setJobGroup(jobGroup);
		if(irpTaskService.deleteJob(scheduleJob)) {
			return "sucess";
		}
		return "error";
	}
	
	@RequestMapping("addJob.do")
	@ResponseBody
	public String addJob(HttpServletRequest request,ScheduleJob scheduleJob) {
		try {
			boolean flag = irpTaskService.saveScheduleJob(scheduleJob);
			if(flag) {
				return "success";
			}
			return "error";
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return "error";
	}
}
