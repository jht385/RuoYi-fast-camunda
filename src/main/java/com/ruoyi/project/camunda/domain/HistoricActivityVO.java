package com.ruoyi.project.camunda.domain;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoricActivityVO extends HistoricActivityInstanceEntity {
  private String assigneeName;
  private String description;
}
