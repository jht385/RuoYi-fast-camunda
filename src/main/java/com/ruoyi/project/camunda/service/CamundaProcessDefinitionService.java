package com.ruoyi.project.camunda.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.github.pagehelper.Page;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.common.utils.text.Convert;
import com.ruoyi.framework.web.page.PageDomain;
import com.ruoyi.framework.web.page.TableSupport;
import com.ruoyi.project.camunda.domain.ProcessDefinitionEx;
import com.ruoyi.project.camunda.mapper.ProcessDefinitionMapper;

import cn.hutool.core.util.StrUtil;

@Transactional
@Service
public class CamundaProcessDefinitionService {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;
    @Autowired
    private FormService formService;

    // 不能用原生sql，Description在act_ge_bytearray的二进制文件里，不是表字段，sql获取不到
    public List<ProcessDefinitionEx> selectProcessDefinitionList(ProcessDefinitionEx processDefinition) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionId()
                .desc();
        // 前端传入的查询条件
        if (StringUtils.isNotBlank(processDefinition.getName())) {
            processDefinitionQuery.processDefinitionNameLike("%" + processDefinition.getName() + "%");
        }
        if (StringUtils.isNotBlank(processDefinition.getKey())) {
            processDefinitionQuery.processDefinitionKeyLike("%" + processDefinition.getKey() + "%");
        }

        List<ProcessDefinition> processDefinitionList;
        if (pageNum != null && pageSize != null) {
            // camunda的pageNum和pagehelper的差1
            processDefinitionList = processDefinitionQuery.listPage((pageNum - 1) * pageSize, pageSize);
        } else {
            processDefinitionList = processDefinitionQuery.list();
        }

        Page<ProcessDefinitionEx> list = new Page<>();
        list.setTotal(processDefinitionQuery.count());
        list.setPageNum(pageNum);
        list.setPageSize(pageSize);

        for (ProcessDefinition definition : processDefinitionList) {
            ProcessDefinitionEntity entityImpl = (ProcessDefinitionEntity) definition;
            ProcessDefinitionEx entity = new ProcessDefinitionEx();
            entity.setId(definition.getId());
            entity.setKey(definition.getKey());
            entity.setName(definition.getName());
            entity.setCategory(definition.getCategory());
            entity.setVersion(definition.getVersion());
            entity.setDescription(definition.getDescription());
            entity.setDeploymentId(definition.getDeploymentId());
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(definition.getDeploymentId())
                    .singleResult();
            entity.setDeploymentTime(deployment.getDeploymentTime());
            entity.setDiagramResourceName(definition.getDiagramResourceName());
            entity.setResourceName(definition.getResourceName());
            entity.setSuspendState(String.valueOf(entityImpl.getSuspensionState()));
            list.add(entity);
        }
        return list;
    }

    public List<ProcessDefinitionEx> selectProcessDefinitionNewestVersionList(ProcessDefinitionEx processDefinition) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        Long userId = ShiroUtils.getUserId();

        ProcessDefinitionQuery processDefinitionQuery;
        if (userId == 1L) {
            processDefinitionQuery = repositoryService
                    .createProcessDefinitionQuery()
                    .latestVersion()
                    .active();
        } else {
            String[] userHasRoles = processDefinitionMapper.selectProcdefKeysByUserId(userId);

            processDefinitionQuery = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionKeysIn(userHasRoles)
                    .latestVersion()
                    .active();
        }

        // 前端传入的查询条件
        String nameLike = processDefinition.getName();
        if (StrUtil.isNotBlank(nameLike)) {
            processDefinitionQuery.processDefinitionNameLike("%" + nameLike + "%");
        }
        List<ProcessDefinition> processDefinitions = processDefinitionQuery
                .orderByProcessDefinitionName().asc()
                .listPage((pageNum - 1) * pageSize, pageSize);

        Page<ProcessDefinitionEx> list = new Page<>();
        list.setTotal(processDefinitionQuery.count());
        list.setPageNum(pageNum);
        list.setPageSize(pageSize);

        for (ProcessDefinition definition : processDefinitions) {
            ProcessDefinitionEntity entityImpl = (ProcessDefinitionEntity) definition;
            ProcessDefinitionEx entity = new ProcessDefinitionEx();
            entity.setId(definition.getId());
            entity.setKey(definition.getKey());
            entity.setName(definition.getName());
            entity.setCategory(definition.getCategory());
            entity.setVersion(definition.getVersion());
            entity.setDescription(definition.getDescription());
            entity.setDeploymentId(definition.getDeploymentId());
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(definition.getDeploymentId())
                    .singleResult();
            entity.setDeploymentTime(deployment.getDeploymentTime());
            entity.setDiagramResourceName(definition.getDiagramResourceName());
            entity.setResourceName(definition.getResourceName());
            entity.setSuspendState(String.valueOf(entityImpl.getSuspensionState()));

            // 选中某个最新版本的流程点击发起跳转到对应的业务表单
            String startFormKey = formService.getStartFormKey(
                    repositoryService.createProcessDefinitionQuery()
                            .processDefinitionId(definition.getId())
                            .singleResult()
                            .getId());
            entity.setStartFormKey(startFormKey);

            list.add(entity);
        }
        return list;
    }

    public void deployProcessDefinition(String filePath) throws FileNotFoundException {
        if (StringUtils.isNotBlank(filePath)) {
            if (filePath.endsWith(".zip")) {
                ZipInputStream inputStream = new ZipInputStream(new FileInputStream(filePath));
                repositoryService.createDeployment()
                        .name(FilenameUtils.getName(filePath))
                        .addZipInputStream(inputStream)
                        .deploy();
            } else if (filePath.endsWith(".bpmn")) {
                repositoryService.createDeployment()
                        .name(FilenameUtils.getName(filePath))
                        .addInputStream(filePath, new FileInputStream(filePath))
                        .deploy();
            }
        }
    }

    public int deleteProcessDeploymentByIds(String deploymentIds) throws Exception {
        String[] deploymentIdsArr = Convert.toStrArray(deploymentIds);
        int counter = 0;
        for (String deploymentId : deploymentIdsArr) {
            List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery()
                    .deploymentId(deploymentId)
                    .list();
            if (!CollectionUtils.isEmpty(instanceList)) {
                throw new Exception("删除失败，存在运行中的流程实例");
            }
            repositoryService.deleteDeployment(deploymentId, true); // true 表示级联删除引用，比如 act_ru_execution 数据
            counter++;
        }
        return counter;
    }

    public void suspendOrActiveApply(String id, String suspendState) {
        if ("1".equals(suspendState)) {
            repositoryService.suspendProcessDefinitionById(id);
        } else if ("2".equals(suspendState)) {
            repositoryService.activateProcessDefinitionById(id);
        }
    }

    public List<ProcessDefinitionEx> selectProcdefKey(Long roleId) {
        return processDefinitionMapper.selectProcdefKey(roleId);
    }

    public void readResource(String processDefinitionId, String resourceName, HttpServletResponse response) {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        InputStream resourceAsStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), resourceName);

        byte[] b = new byte[1024];
        int len = -1;
        try {
            while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getXml(String processDefinitionId) {
        try (InputStream in = repositoryService
                .getProcessModel(processDefinitionId);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            String xml = reader.lines().collect(Collectors.joining("\n"));
            Map<String, String> result = new HashMap<>();
            result.put("bpmn20Xml", xml);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("读取 XML 失败", e);
        }
    }

}
