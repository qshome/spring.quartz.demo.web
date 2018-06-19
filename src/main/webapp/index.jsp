<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="js/jquery-2.0.3.min.js"></script>
<style>
table {
  border-collapse:collapse;
}

table,th, td  {
  border: 1px solid black;
}


</style>
<body>
	<table >
		<thead>
			<tr>
				<th>任务名称</th>
				<th>任务组名称</th>
				<th>任务状态</th>
				<th>时钟表达式</th>
				<th>描述</th>
				<th>操作</th>
			</tr>
		</thead>
		<tbody id="content">
			
		</tbody>
	</table>
	
	<form name="scheduleJob" id="job_form" hidden>
		<label>任务名称：</label> <input name="jobName" type="text">
		<label>任务组名称：</label> <input name="jobGroup" type="text">
		<label>任务状态：</label> <input name="jobStatus" type="text">
		<label>是否并发：</label> <input name="isConcurrent" type="text">
		
		<label>任务描述：</label> <input name="description" type="text"><br/>
		<label>执行对象：</label> <input name="springId" type="text">
		<label>时钟表达式：</label> <input name="cronExpression" type="text">
		<label>执行类名：</label> <input name="jobClass" type="text">
		<label>执行方法：</label> <input name="methodName" type="text">
		<input type="button" value="保存" id="save">
	</form>
	
	<script>
		$(function(){
			refreshTable()
			//暂停
			$("#content").on("click","#pause",function(){
				var tds = $(this).parent().parent().find("td");
				var jobName = $(tds[0]).text();
				var jobGroup = $(tds[1]).text();
				$.post("pauserJob.do",{"jobName":jobName,"jobGroup":jobGroup},function(data){
					refreshTable();
					alert(data);
				})
			})
			//暂停后恢复
			$("#content").on("click","#deleteJob",function(){
				var tds = $(this).parent().parent().find("td");
				var jobName = $(tds[0]).text();
				var jobGroup = $(tds[1]).text();
				$.post("deleteJob.do",{"jobName":jobName,"jobGroup":jobGroup},function(data){
					refreshTable();
					alert(data);
				})
			})
			//删除
			$("#content").on("click","#reschedule",function(){
				var tds = $(this).parent().parent().find("td");
				var jobName = $(tds[0]).text();
				var jobGroup = $(tds[1]).text();
				$.post("resumeJob.do",{"jobName":jobName,"jobGroup":jobGroup},function(data){
					refreshTable();
					alert(data);
				})
				
			})
			//立即执行一次
			$("#content").on("click","#executeJob",function(){
				var tds = $(this).parent().parent().find("td");
				var jobName = $(tds[0]).text();
				var jobGroup = $(tds[1]).text();
				$.post("executeJobNow.do",{"jobName":jobName,"jobGroup":jobGroup},function(data){
					refreshTable();
					alert(data);
				})
				
			})
			
			//新增
			$("#content").on("click","#showView",function(){
				$("#job_form").find("input[name]").val("");
				$("form[name='scheduleJob']").find("input[readonly]").removeAttr("readonly");
				$("#job_form").show();
			})
			
			//更新
			$("#content").on("click","#getUpdateJob",function(){
				$("#job_form").show();
				var tds = $(this).parent().parent().find("td");
				var jobName = $(tds[0]).text();
				var jobGroup = $(tds[1]).text();
				$.post("getJobByJobNameAndJobGroup.do",{"jobName":jobName,"jobGroup":jobGroup},function(data){
					if(data!=null){
						$("form[name='scheduleJob']").find("input[name='jobName']").eq(0).val(data.jobName).attr("readonly",true);
						$("form[name='scheduleJob']").find("input[name='jobGroup']").eq(0).val(data.jobGroup).attr("readonly",true);
						$("form[name='scheduleJob']").find("input[name='jobStatus']").eq(0).val(data.jobStatus).attr("readonly",true);
						$("form[name='scheduleJob']").find("input[name='isConcurrent']").eq(0).val(data.isConcurrent).attr("readonly",true);
						$("form[name='scheduleJob']").find("input[name='description']").eq(0).val(data.description).attr("readonly",true);
						$("form[name='scheduleJob']").find("input[name='springId']").eq(0).val(data.springId).attr("readonly",true);
						$("form[name='scheduleJob']").find("input[name='cronExpression']").eq(0).val(data.cronExpression);
						$("form[name='scheduleJob']").find("input[name='jobClass']").eq(0).val(data.jobClass).attr("readonly",true);
						$("form[name='scheduleJob']").find("input[name='methodName']").eq(0).val(data.methodName).attr("readonly",true);
					}
				})
			})
			
			//保存
			$("#save").on("click",function(){
				var length = $("form[name='scheduleJob']").find("input[readonly]").length;
				if(length>0){//更新
					$.post("updateCronExpression.do", $("#job_form").serialize(),function(data){
						refreshTable();
						alert(data);
					})
				}else{//添加
					$.post("addJob.do", $("#job_form").serialize(),function(data){
						refreshTable();
						alert(data);
					})
				}
			})
		})
		function refreshTable(){
			$("#content").empty();
			$.post("query.do",{},function(data){
				$.each(data,function(i,single){
					var c='<tr>'
					+'<td>'+single.jobName+'</td>'
					+'<td>'+single.jobGroup+'</td>'
					+'<td>'+single.jobStatus+'</td>'
					+'<td>'+single.cronExpression+'</td>'
					+'<td>'+single.description+'</td>'
					+'<td><input type="button" value="暂停" id="pause" >'
					+'<input type="button" value="恢复" id="reschedule" >'
					+'<input type="button" value="删除" id="deleteJob" >'
					+'<input type="button" value="立即执行一次" id="executeJob" >'
					+'<input type="button" value="更新" id="getUpdateJob" >'
					+'<input type="button" value="新增" id="showView" ></td>'
					+'</tr>'
					$("#content").append(c);
				})
			})
		}
	</script>
</body>
</html>
