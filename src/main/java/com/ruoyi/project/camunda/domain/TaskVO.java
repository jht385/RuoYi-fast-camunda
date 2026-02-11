package com.ruoyi.project.camunda.domain;

import java.util.Date;

import lombok.Data;

@Data
public class TaskVO {
	private String taskId;
	private String executionId;
	private String procInstId;
	private String procDefId;
	private String taskName;
	private String description;
	private String taskDefKey;
	private String priority;
	private Date createTime;
	private Integer taskStartTimeOverDay;
	private Integer suspensionState;
	private String taskFormKey;
	private String procDefName;
	private String procDefKey;
	private String processInstanceName;
	//
	private String userId;
	private boolean isAdmin;
	private Date startTime;
	private String businessKey;
}
