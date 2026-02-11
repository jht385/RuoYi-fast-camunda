package com.ruoyi.project.camunda.service;

public interface ICommonProcessService<T> {
	int processFormStart(T t);

	int handle(T t);

	int draft(T t);

	int processFormEdit(T t);
}
