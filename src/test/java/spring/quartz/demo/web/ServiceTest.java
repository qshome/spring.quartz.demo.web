package spring.quartz.demo.web;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.spring.quartz.dao.ScheduleJobDao;
import com.spring.quartz.entity.ScheduleJob;
import com.spring.quartz.service.IrpTaskService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-context.xml",
"classpath:spring-mvc.xml" })
public class ServiceTest {
	@Autowired
	private ScheduleJobDao  scheduleJobDao;
	@Autowired
	private IrpTaskService irpTaskService;
	@Test
	public void test(){
		List<ScheduleJob>  list=(List<ScheduleJob>) scheduleJobDao.find("from ScheduleJob where jobStatus ='1'");
		System.out.println(list+"");
		
		//List<ScheduleJob>  list =irpTaskService.getTaskList();
		//System.out.println(list+"----------");
	}
	
}
