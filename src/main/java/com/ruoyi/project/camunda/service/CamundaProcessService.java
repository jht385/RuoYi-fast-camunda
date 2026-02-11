package com.ruoyi.project.camunda.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.framework.web.page.PageDomain;
import com.ruoyi.framework.web.page.TableSupport;
import com.ruoyi.project.camunda.domain.HistoricActivityVO;
import com.ruoyi.project.camunda.domain.ProcinstVO;
import com.ruoyi.project.camunda.domain.TaskVO;
import com.ruoyi.project.camunda.mapper.ProcessMapper;
import com.ruoyi.project.system.user.domain.User;
import com.ruoyi.project.system.user.mapper.UserMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class CamundaProcessService {
    @Autowired
    private ProcessMapper processMapper;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private FormService formService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ProcessInstance simpleStart(String applyUserId, String businessKey, String processInstanceName,
            String processDefinitionKey, Map<String, Object> variables) {
        identityService.setAuthenticatedUserId(applyUserId); // identitylink表，act_ru_execution，act_hi_procinst对应列
        if (StringUtils.isNotBlank(processInstanceName)) { // 用于之后查询各种类型processDefinitionKey的业务，给个实例名查询用
            variables.put("processInstanceName", processInstanceName);
        }
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey,
                variables);
        return instance;
    }

    public Task getProcessInstanceSingleTask(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    }

    public void saveOrUpdateTask(Task task) {
        taskService.saveTask(task); // 无记录添加，有记录更新
    }

    public Comment createComment(String taskId, String processInstanceId, String message) {
        return taskService.createComment(taskId, processInstanceId, message);
    }

    public void completeTask(String taskId, String applyUserId, Map<String, Object> variables) {
        taskService.setAssignee(taskId, applyUserId);
        taskService.complete(taskId, variables);
    }

    public List<TaskVO> todoList(TaskVO task) {
        task.setUserId(String.valueOf(ShiroUtils.getUserId()));
        task.setAdmin(ShiroUtils.getUserId() == 1L);
        List<TaskVO> list = processMapper.todoList(task);

        for (TaskVO taskVO : list) {
            String formKey;

            formKey = formService.getTaskFormKey(
                    taskVO.getProcDefId(),
                    taskVO.getTaskDefKey());

            if (StringUtils.isEmpty(formKey)) {
                formKey = formService.getStartFormKey(
                        repositoryService.createProcessDefinitionQuery()
                                .processDefinitionId(taskVO.getProcDefId())
                                .singleResult()
                                .getId());
            }

            taskVO.setTaskFormKey(formKey);
        }

        return list;
    }

    public Map<String, Object> getHistoryView(String procInstId) {
        HistoricProcessInstance instance = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(procInstId)
                .singleResult();

        if (instance == null) {
            throw new RuntimeException("实例不存在: " + procInstId);
        }

        String definitionId = instance.getProcessDefinitionId();
        String xml = getXmlByProcessDefinitionId(definitionId);

        List<HistoricActivityInstance> activities = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(procInstId)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();

        List<String> finishedIds = new ArrayList<>();
        List<String> activeIds = new ArrayList<>();

        activities.forEach(activity -> {
            if (activity.getEndTime() != null) {
                finishedIds.add(activity.getActivityId());
            } else {
                activeIds.add(activity.getActivityId());
            }
        });

        Map<String, Object> result = new HashMap<>();
        result.put("processInstanceId", procInstId);
        result.put("processDefinitionId", definitionId);
        result.put("bpmn20Xml", xml);
        result.put("finishedActivityIds", finishedIds);
        result.put("activeActivityIds", activeIds);
        return result;
    }

    private String getXmlByProcessDefinitionId(String definitionId) {
        try (InputStream in = repositoryService.getProcessModel(definitionId);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toString("UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("读取 XML 失败", e);
        }
    }

    public List<HistoricActivityVO> listHistory(HistoricActivityVO historicActivityVO) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        List<HistoricActivityVO> activityList = new ArrayList<>();

        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(historicActivityVO.getProcessInstanceId())
                .activityType("userTask")
                .finished().orderByHistoricActivityInstanceStartTime().asc()
                .listPage((pageNum - 1) * pageSize, pageSize);

        list.forEach(historicActivityInstance -> {
            HistoricActivityVO activity = new HistoricActivityVO();

            BeanUtils.copyProperties(historicActivityInstance, activity);

            String taskId = historicActivityInstance.getTaskId();
            if (StringUtils.isNotEmpty(taskId)) {
                HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                        .taskId(taskId).singleResult();
                activity.setDescription(historicTaskInstance.getDescription());
            }

            User sysUser = userMapper.selectUserById(Long.parseLong(historicActivityInstance.getAssignee()));
            if (sysUser != null) {
                activity.setAssigneeName(sysUser.getUserName());
            }

            activityList.add(activity);
        });
        return activityList;
    }

    public int updateHistoryTaskDescription(Map<String, Object> map) {
        String taskId = (String) map.get("taskId");
        String desc = (String) map.get("desc");
        String sql = "UPDATE act_hi_taskinst SET DESCRIPTION_ = ? WHERE ID_ = ?";
        return jdbcTemplate.update(sql, desc, taskId);
    }

    public int updateHistoryVariable(String processInstanceId, String variableName, String textValue) {
        String sql = "UPDATE act_hi_varinst " +
                "SET TEXT_ = ? " +
                "WHERE PROC_INST_ID_ = ? " +
                "AND NAME_ = ?";
        return jdbcTemplate.update(sql, textValue, processInstanceId, variableName);
    }

    public List<Map<String, Object>> getProcessType() {
        String sql = "select distinct KEY_, NAME_ from act_re_procdef";
        return jdbcTemplate.queryForList(sql);
    }

    public Task getTaskById(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    public List<ProcinstVO> doneList(ProcinstVO procinstVO) {
        List<ProcinstVO> list = processMapper.doneList(procinstVO);

        for (ProcinstVO temp : list) {
            String formKey;

            formKey = formService.getTaskFormKey(
                    temp.getProcDefId(),
                    temp.getTaskDefKey());

            if (StringUtils.isEmpty(formKey)) {
                formKey = formService.getStartFormKey(
                        repositoryService.createProcessDefinitionQuery()
                                .processDefinitionId(temp.getProcDefId())
                                .singleResult()
                                .getId());
            }

            temp.setFormKey(formKey);
        }

        return list;
    }

}
