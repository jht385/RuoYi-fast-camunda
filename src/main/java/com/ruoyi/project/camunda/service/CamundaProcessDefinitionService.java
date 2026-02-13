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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.common.utils.text.Convert;
import com.ruoyi.framework.web.page.PageDomain;
import com.ruoyi.framework.web.page.TableSupport;
import com.ruoyi.project.camunda.domain.ProcessDefinitionEx;
import com.ruoyi.project.camunda.mapper.ProcessDefinitionMapper;

import cn.hutool.core.util.StrUtil;

@Transactional(rollbackFor = Exception.class)
@Service
public class CamundaProcessDefinitionService {

    /** 流程定义列表无分页时的默认每页条数上限，避免全量加载 */
    private static final int DEFAULT_PAGE_SIZE_MAX = 500;
    /** 超级管理员用户 ID（拥有全部流程权限），可按需改为配置项 */
    private static final long ADMIN_USER_ID = 1L;

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;
    @Autowired
    private FormService formService;

    // 不能用原生sql，Description在act_ge_bytearray的二进制文件里，不是表字段，sql获取不到
    public List<ProcessDefinitionEx> selectProcessDefinitionList(
            ProcessDefinitionEx processDefinition) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        ProcessDefinitionQuery processDefinitionQuery = repositoryService
                .createProcessDefinitionQuery().orderByProcessDefinitionId().desc();
        // 前端传入的查询条件
        if (StringUtils.isNotBlank(processDefinition.getName())) {
            processDefinitionQuery
                    .processDefinitionNameLike("%" + processDefinition.getName() + "%");
        }
        if (StringUtils.isNotBlank(processDefinition.getKey())) {
            processDefinitionQuery.processDefinitionKeyLike("%" + processDefinition.getKey() + "%");
        }

        int pn = (pageNum != null && pageNum > 0) ? pageNum : 1;
        int ps = (pageSize != null && pageSize > 0) ? pageSize : 10;
        if (pageNum == null || pageSize == null) {
            ps = Math.min(ps, DEFAULT_PAGE_SIZE_MAX);
        }
        // camunda 的 pageNum 和 pagehelper 的差 1
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.listPage((pn - 1) * ps, ps);

        Page<ProcessDefinitionEx> list = new Page<>();
        list.setTotal(processDefinitionQuery.count());
        list.setPageNum(pn);
        list.setPageSize(ps);

        // 批量查询 deployment，避免 N+1
        Map<String, Deployment> deploymentMap = repositoryService.createDeploymentQuery().list()
                .stream().collect(Collectors.toMap(Deployment::getId, d -> d));

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
            Deployment deployment = deploymentMap.get(definition.getDeploymentId());
            if (deployment != null) {
                entity.setDeploymentTime(deployment.getDeploymentTime());
            }
            entity.setDiagramResourceName(definition.getDiagramResourceName());
            entity.setResourceName(definition.getResourceName());
            entity.setSuspendState(String.valueOf(entityImpl.getSuspensionState()));
            list.add(entity);
        }
        return list;
    }

    public List<ProcessDefinitionEx> selectProcessDefinitionNewestVersionList(
            ProcessDefinitionEx processDefinition) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        Long userId = ShiroUtils.getUserId();

        ProcessDefinitionQuery processDefinitionQuery;
        if (ADMIN_USER_ID == userId) {
            processDefinitionQuery = repositoryService.createProcessDefinitionQuery().latestVersion().active();
        } else {
            String[] userHasRoles = processDefinitionMapper.selectProcdefKeysByUserId(userId);

            processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKeysIn(userHasRoles).latestVersion().active();
        }

        // 前端传入的查询条件
        String nameLike = processDefinition.getName();
        if (StrUtil.isNotBlank(nameLike)) {
            processDefinitionQuery.processDefinitionNameLike("%" + nameLike + "%");
        }
        int pn = (pageNum != null && pageNum > 0) ? pageNum : 1;
        int ps = (pageSize != null && pageSize > 0) ? pageSize : 10;
        List<ProcessDefinition> processDefinitions = processDefinitionQuery
                .orderByProcessDefinitionName().asc().listPage((pn - 1) * ps, ps);

        Page<ProcessDefinitionEx> list = new Page<>();
        list.setTotal(processDefinitionQuery.count());
        list.setPageNum(pn);
        list.setPageSize(ps);

        // 批量查询 deployment，避免 N+1
        Map<String, Deployment> deploymentMap = repositoryService.createDeploymentQuery().list()
                .stream().collect(Collectors.toMap(Deployment::getId, d -> d));

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
            Deployment deployment = deploymentMap.get(definition.getDeploymentId());
            if (deployment != null) {
                entity.setDeploymentTime(deployment.getDeploymentTime());
            }
            entity.setDiagramResourceName(definition.getDiagramResourceName());
            entity.setResourceName(definition.getResourceName());
            entity.setSuspendState(String.valueOf(entityImpl.getSuspensionState()));
            // 直接使用 definition.getId()，避免额外查询
            entity.setStartFormKey(formService.getStartFormKey(definition.getId()));

            list.add(entity);
        }
        return list;
    }

    public void deployProcessDefinition(String filePath) throws FileNotFoundException {
        if (StringUtils.isNotBlank(filePath)) {
            if (filePath.endsWith(".zip")) {
                ZipInputStream inputStream = new ZipInputStream(new FileInputStream(filePath));
                repositoryService.createDeployment().name(FilenameUtils.getName(filePath))
                        .addZipInputStream(inputStream).deploy();
            } else if (filePath.endsWith(".bpmn")) {
                repositoryService.createDeployment().name(FilenameUtils.getName(filePath))
                        .addInputStream(filePath, new FileInputStream(filePath)).deploy();
            }
        }
    }

    public int deleteProcessDeploymentByIds(String deploymentIds) throws Exception {
        String[] deploymentIdsArr = Convert.toStrArray(deploymentIds);
        int counter = 0;
        for (String deploymentId : deploymentIdsArr) {
            long runningCount = runtimeService.createProcessInstanceQuery().deploymentId(deploymentId).count();
            if (runningCount > 0) {
                throw new Exception("删除失败，存在运行中的流程实例");
            }
            repositoryService.deleteDeployment(deploymentId, true); // true 表示级联删除引用，比如
                                                                    // act_ru_execution 数据
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

    public void readResource(String processDefinitionId, String resourceName,
            HttpServletResponse response) {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();

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
        try (InputStream in = repositoryService.getProcessModel(processDefinitionId);
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
