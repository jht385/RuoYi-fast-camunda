package com.ruoyi.project.camunda.mapper;

import java.util.List;

import com.ruoyi.project.camunda.domain.ProcinstVO;
import com.ruoyi.project.camunda.domain.TaskVO;

public interface ProcessMapper {

  List<TaskVO> todoList(TaskVO task);

  List<ProcinstVO> doneList(ProcinstVO procinstVO);

}
