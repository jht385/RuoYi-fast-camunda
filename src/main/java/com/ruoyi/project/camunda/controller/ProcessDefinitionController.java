package com.ruoyi.project.camunda.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.camunda.domain.ProcessDefinitionEx;
import com.ruoyi.project.camunda.service.CamundaProcessDefinitionService;
import com.ruoyi.project.system.config.service.IConfigService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("definition")
@Slf4j
public class ProcessDefinitionController extends BaseController {

    @Autowired
    private CamundaProcessDefinitionService processDefinitionService;
    @Autowired
    private IConfigService configService;

    @GetMapping
    public String processDefinition(ModelMap mmap) {
        mmap.put("bpmnOnlineUrl", configService.selectConfigByKey("bpmn.online.url"));
        return "camunda/definition";
    }

    @GetMapping("/newestVersion")
    public String newestVersion(ModelMap mmap) {
        return "camunda/newestVersion";
    }

    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ProcessDefinitionEx processDefinition) {
        List<ProcessDefinitionEx> list = processDefinitionService.selectProcessDefinitionList(processDefinition);
        return getDataTable(list);
    }

    @PostMapping("/newestVersionList")
    @ResponseBody
    public TableDataInfo newestVersionList(ProcessDefinitionEx processDefinition) {
        List<ProcessDefinitionEx> list = processDefinitionService
                .selectProcessDefinitionNewestVersionList(processDefinition);
        return getDataTable(list);
    }

    @RequestMapping(value = "/viewDefinitionImg")
    public String viewDefinitionImg(ModelMap mmap, String procdefId) {
        mmap.put("action", "viewDefinitionImg");
        mmap.put("procdefId", procdefId);
        return "camunda/modeler-view";
    }

    @Log(title = "流程定义", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    @ResponseBody
    public AjaxResult upload(@RequestParam("processDefinition") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                String extensionName = file.getOriginalFilename()
                        .substring(file.getOriginalFilename().lastIndexOf('.') + 1);
                if (!"bpmn".equalsIgnoreCase(extensionName)
                        && !"zip".equalsIgnoreCase(extensionName)) {
                    return error("流程定义文件仅支持 bpmn zip 格式！");
                }
                // p.s. 此时 FileUploadUtils.upload() 返回字符串 fileName 前缀为
                // Constants.RESOURCE_PREFIX，需剔除
                // 详见: FileUploadUtils.getPathFileName(...)
                String fileName = FileUploadUtils.upload(RuoYiConfig.getProfile() + "/processDefiniton", file);
                if (StringUtils.isNotBlank(fileName)) {
                    String realFilePath = RuoYiConfig.getProfile()
                            + fileName.substring(Constants.RESOURCE_PREFIX.length());
                    processDefinitionService.deployProcessDefinition(realFilePath);
                    return success();
                }
            }
            return error("不允许上传空文件！");
        } catch (Exception e) {
            log.error("上传流程定义文件失败！", e);
            return error(e.getMessage());
        }
    }

    @Log(title = "流程定义", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        try {
            return toAjax(processDefinitionService.deleteProcessDeploymentByIds(ids));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @Log(title = "流程定义", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export() {
        List<ProcessDefinitionEx> list = processDefinitionService
                .selectProcessDefinitionList(new ProcessDefinitionEx());
        ExcelUtil<ProcessDefinitionEx> util = new ExcelUtil<>(ProcessDefinitionEx.class);
        return util.exportExcel(list, "流程定义数据");
    }

    @PostMapping("/suspendOrActiveApply")
    @ResponseBody
    public AjaxResult suspendOrActiveApply(String id, String suspendState) {
        processDefinitionService.suspendOrActiveApply(id, suspendState);
        return success();
    }

    /**
     * 读取流程资源
     *
     * @param processDefinitionId 流程定义ID
     * @param resourceName        资源名称
     */
    @RequestMapping(value = "/readResource")
    public void readResource(@RequestParam("processDefinitionId") String processDefinitionId,
            @RequestParam("resourceName") String resourceName, HttpServletResponse response) {
        processDefinitionService.readResource(processDefinitionId, resourceName, response);
    }

    @GetMapping(value = "/{processDefinitionId}/xml")
    @ResponseBody
    public AjaxResult getXml(@PathVariable String processDefinitionId) {
        return success(processDefinitionService.getXml(processDefinitionId));
    }

}
