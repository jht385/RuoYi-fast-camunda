package com.ruoyi.project.camunda.domain;

import java.util.Map;

import lombok.Data;

@Data
public class ProcinstVO {
	private String id;
	private String procInstId;
	private String businessKey;
	private String procDefKey;
	private String procDefId;
	private String startTime;
	private String endTime;
	private String processInstanceName;
	private String taskId;
	private String taskName;
	private Integer taskStartTimeOverDay;
	private String taskDefKey;
	private String procDefName;
	private String deploymentId;
	//
	private String formKey;
	private String pass;
	private Long userId;
	private Map<String, Object> params;
}
