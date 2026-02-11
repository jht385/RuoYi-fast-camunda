package com.ruoyi.project.camunda.mapper;

import java.util.List;
import java.util.Map;

import com.ruoyi.project.camunda.domain.ProcessDefinitionEx;

public interface ProcessDefinitionMapper {

  public List<ProcessDefinitionEx> selectProcessDefinitionList(ProcessDefinitionEx processDefinition);

  public List<ProcessDefinitionEx> selectProcdefKey(Long roleId);

  public int batchProcdefKey(List<Map<String,Object>> list);

  public int deleteProcdefKeyByRoleId(Long roleId);

  public String[] selectProcdefKeysByUserId(Long userId);

}
