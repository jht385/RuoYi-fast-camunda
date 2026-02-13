package com.ruoyi.project.oa.qingjia.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.common.utils.text.Convert;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.project.camunda.service.CamundaProcessService;
import com.ruoyi.project.oa.qingjia.domain.OaQingjia;
import com.ruoyi.project.oa.qingjia.mapper.OaQingjiaMapper;
import com.ruoyi.project.oa.qingjia.service.IOaQingjiaService;
import com.ruoyi.project.system.role.mapper.RoleExMapper;
import com.ruoyi.project.system.user.domain.User;
import com.ruoyi.project.system.user.mapper.UserExMapper;

import cn.hutool.extra.mail.MailUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 请假Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-08
 */
@Slf4j
@Service
public class OaQingjiaServiceImpl implements IOaQingjiaService {
    private static final String processDefinitionKey = "qingjia";

    @Autowired
    private RuoYiConfig ruoYiConfig;
    @Autowired
    private OaQingjiaMapper bizMapper;
    @Autowired
    private CamundaProcessService processService;
    @Autowired
    private UserExMapper userExMapper;
    @Autowired
    private RoleExMapper roleExMapper;

    /**
     * 查询请假
     * 
     * @param id 请假主键
     * @return 请假
     */
    @Override
    public OaQingjia selectOaQingjiaById(String id) {
        return bizMapper.selectOaQingjiaById(id);
    }

    /**
     * 查询请假列表
     * 
     * @param oaQingjia 请假
     * @return 请假
     */
    @Override
    public List<OaQingjia> selectOaQingjiaList(OaQingjia oaQingjia) {
        return bizMapper.selectOaQingjiaList(oaQingjia);
    }

    /**
     * 新增请假
     * 
     * @param oaQingjia 请假
     * @return 结果
     */
    @Override
    public int insertOaQingjia(OaQingjia oaQingjia) {
        oaQingjia.setCreateTime(DateUtils.getNowDate());
        return bizMapper.insertOaQingjia(oaQingjia);
    }

    /**
     * 修改请假
     * 
     * @param oaQingjia 请假
     * @return 结果
     */
    @Override
    public int updateOaQingjia(OaQingjia oaQingjia) {
        oaQingjia.setUpdateTime(DateUtils.getNowDate());
        return bizMapper.updateOaQingjia(oaQingjia);
    }

    /**
     * 批量删除请假
     * 
     * @param ids 需要删除的请假主键
     * @return 结果
     */
    @Override
    public int deleteOaQingjiaByIds(String ids) {
        return bizMapper.deleteOaQingjiaByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除请假信息
     * 
     * @param id 请假主键
     * @return 结果
     */
    @Override
    public int deleteOaQingjiaById(String id) {
        return bizMapper.deleteOaQingjiaById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int processFormStart(OaQingjia t) {
        String applyUserId = ShiroUtils.getUserId().toString();
        String businessKey = t.getProcessId(); // 实体类 ID，作为流程的业务 key
        String nextMsg = t.getNextMsg();
        Integer pass = t.getPass();

        Map<String, Object> variables = new HashMap<String, Object>();

        String processInstanceName = ShiroUtils.getSysUser().getUserName() + "-" + t.getName() + "-"
                + DateUtils.dateTime(t.getQuitDate()) + "-" + t.getDay();

        // start

        // n1节点如果能返回，需要配置Candidate，配置了Candidate就必须传值
        variables.put("users", ""); // 配置下一节点候选用户，模板写了这里不设置变量会报错
        variables.put("groups", ""); // 配置下一阶段候选组

        ProcessInstance processInstance = processService.simpleStart(applyUserId, businessKey,
                processInstanceName, processDefinitionKey, variables);

        // n1
        String processInstanceId = processInstance.getId();
        Task task = processService.getProcessInstanceSingleTask(processInstanceId);

        // 这个api添加评论适合单节点可以不停评论的场景
        // processService.createComment(task.getId(), processInstanceId, nextMsg);
        task.setDescription(nextMsg);
        processService.saveOrUpdateTask(task);

        String taskDefinitionKey = task.getTaskDefinitionKey();
        String groups = getGroupByForwardTaskDefinitionKey(taskDefinitionKey);

        variables.clear();

        variables.put("day", t.getDay());

        variables.put("users", ""); // 配置下一节点候选用户，模板写了这里不设置变量会报错
        variables.put("groups", groups); // 配置下一阶段候选组
        variables.put("pass", pass);
        processService.completeTask(task.getId(), applyUserId, variables);

        Long[] userIds = t.getNextUser();
        // List<User> userList = userExMapper.listUserByUserIds(userIds);
        List<User> userList = userExMapper.listUserByRoleIds(groups);

        StringBuilder contentSb = new StringBuilder();
        contentSb.append("名字为：" + processInstanceName + " 的流程需要处理\n");
        contentSb.append("\n" + nextMsg + "\n");
        contentSb.append("请登录 http://" + ruoYiConfig.getAppServer() + "/ 查看");

        String mailTo = "";
        String title = "请假流程：" + processInstanceName + "需要处理 ";
        String content = contentSb.toString();

        StringBuilder sb = new StringBuilder();
        for (User user : userList) {
            if (!StringUtils.isEmpty(user.getEmail())) {
                sb.append(user.getEmail() + ";");
            }
        }
        mailTo = sb.toString();

        log.debug("邮件记录\n发邮件给：" + mailTo + "\n标题：" + title + "\n内容：" + content);
        if ("pro".equals(ruoYiConfig.getActive())) {
            try {
                MailUtil.send(mailTo, title, content, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        t.setId(t.getProcessId());
        t.setStatus("1");
        t.setProcInstId(processInstanceId);
        t.setCreateBy(applyUserId);
        t.setCreateTime(DateUtils.getNowDate());
        int cnt = bizMapper.insertOaQingjia(t);

        return cnt;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int handle(OaQingjia t) {
        String applyUserId = ShiroUtils.getUserId().toString();
        String nextMsg = t.getNextMsg();
        Integer pass = t.getPass();
        String taskId = t.getTaskId();

        Map<String, Object> variables = new HashMap<String, Object>();

        Task task = processService.getTaskById(taskId);
        String taskDefinitionKey = task.getTaskDefinitionKey();

        String groups = "";

        String processInstanceName = "";

        if (pass == 0) { // 拒绝
            task.setDescription(nextMsg);
            processService.saveOrUpdateTask(task);

            variables.put("pass", pass);
            processService.completeTask(task.getId(), applyUserId, variables);

            t.setStatus("3");
            bizMapper.updateOaQingjia(t);
            return 1;
        } else if (pass == 1) { // 通过
            task.setDescription(nextMsg);
            processService.saveOrUpdateTask(task);

            groups = getGroupByForwardTaskDefinitionKey(taskDefinitionKey);

            if ("n21".equals(taskDefinitionKey) || "n3".equals(taskDefinitionKey)) {
                t.setStatus("2");
            }

            // 节点有更新到processInstanceName字段的也要更新
            // processInstanceName = ShiroUtils.getSysUser().getUserName() + "-" +
            // t.getName() + "-"
            // + DateUtils.dateTime(t.getQuitDate())
            // + "-" + t.getDay();
            // processService.updateHistoryVariable(t.getProcInstId(),
            // "processInstanceName", processInstanceName);
        } else if (pass == 2) { // 返回
            task.setDescription(nextMsg);
            processService.saveOrUpdateTask(task);

            groups = getGroupByReverseTaskDefinitionKey(taskDefinitionKey);
        }

        t.setId(t.getProcessId());
        int cnt = bizMapper.updateOaQingjia(t);

        variables.clear();
        variables.put("users", ""); // 配置下一节点候选用户，模板写了这里不设置变量会报错
        variables.put("groups", groups); // 配置下一阶段候选组
        variables.put("pass", pass);
        processService.completeTask(task.getId(), applyUserId, variables);

        Long[] userIds = t.getNextUser();
        List<User> userList = null;
        // List<User> userList = userExMapper.listUserByUserIds(userIds);
        if (StringUtils.isNotEmpty(groups)) {
            userList = userExMapper.listUserByRoleIds(groups);
        } else {
            userList = new ArrayList<User>();
        }

        StringBuilder contentSb = new StringBuilder();
        contentSb.append("名字为：" + processInstanceName + " 的流程需要处理\n返修列表如下：\n");
        contentSb.append("\n" + nextMsg + "\n");
        contentSb.append("请登录 http://" + ruoYiConfig.getAppServer() + "/ 查看");

        String mailTo = "";
        String title = "请假流程：" + processInstanceName + "需要处理 ";
        String content = contentSb.toString();

        StringBuilder sb = new StringBuilder();
        for (User user : userList) {
            if (!StringUtils.isEmpty(user.getEmail())) {
                sb.append(user.getEmail() + ";");
            }
        }
        mailTo = sb.toString();

        log.debug("邮件记录\n发邮件给：" + mailTo + "\n标题：" + title + "\n内容：" + content);
        if ("pro".equals(ruoYiConfig.getActive())) {
            try {
                MailUtil.send(mailTo, title, content, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return cnt;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int draft(OaQingjia t) {
        String processInstanceName = ShiroUtils.getSysUser().getUserName() + "-" + t.getName() + "-"
                + DateUtils.dateTime(t.getQuitDate()) + "-" + t.getDay();
        processService.updateHistoryVariable(t.getProcInstId(), "processInstanceName",
                processInstanceName);

        t.setId(t.getProcessId());
        t.setUpdateBy(String.valueOf(ShiroUtils.getUserId()));
        t.setUpdateTime(DateUtils.getNowDate());
        return updateOaQingjia(t);
    }

    @Override
    public int processFormEdit(OaQingjia t) {
        String processInstanceName = ShiroUtils.getSysUser().getUserName() + "-" + t.getName() + "-"
                + DateUtils.dateTime(t.getQuitDate()) + "-" + t.getDay();
        processService.updateHistoryVariable(t.getProcInstId(), "processInstanceName",
                processInstanceName);

        t.setId(t.getProcessId());
        t.setUpdateBy(String.valueOf(ShiroUtils.getUserId()));
        t.setUpdateTime(DateUtils.getNowDate());
        return updateOaQingjia(t);
    }

    // 正向流转
    public String getGroupByForwardTaskDefinitionKey(String taskDefinitionKey) {
        String groups = "";
        switch (taskDefinitionKey) {
            case "n1":
                groups = roleExMapper.getRoleIdsByRoleKeys("leader");
                break;
            case "n22":
                groups = roleExMapper.getRoleIdsByRoleKeys("ceo");
                break;
            default:
                break;
        }
        return groups;
    }

    // 反向流转
    public String getGroupByReverseTaskDefinitionKey(String taskDefinitionKey) {
        String groups = "";
        switch (taskDefinitionKey) {
            case "n3":
                groups = roleExMapper.getRoleIdsByRoleKeys("leader");
                break;
            default:
                break;
        }
        return groups;
    }

}
