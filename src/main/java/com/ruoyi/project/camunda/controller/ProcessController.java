package com.ruoyi.project.camunda.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.camunda.domain.HistoricActivityVO;
import com.ruoyi.project.camunda.domain.ProcinstVO;
import com.ruoyi.project.camunda.domain.TaskVO;
import com.ruoyi.project.camunda.service.CamundaProcessService;

@Controller
@RequestMapping("process")
public class ProcessController extends BaseController {

    @Autowired
    private CamundaProcessService processService;

    @GetMapping("/todo")
    public String todo(ModelMap mmap) {
        return "camunda/todo";
    }

    @PostMapping("/todoList")
    @ResponseBody
    public TableDataInfo todoList(TaskVO taskVO) {
        startPage();
        List<TaskVO> list = processService.todoList(taskVO);
        return getDataTable(list);
    }

    @GetMapping("/done")
    public String newestVersion(ModelMap mmap) {
        mmap.put("processTypeList", processService.getProcessType());
        return "camunda/done";
    }

    @PostMapping("/doneList")
    @ResponseBody
    public TableDataInfo doneList(ProcinstVO procinstVO) {
        startPage();
        List<ProcinstVO> list = processService.doneList(procinstVO);
        return getDataTable(list);
    }

    @RequestMapping(value = "/viewProcessingImg")
    public String viewProcessingImg(ModelMap mmap, String procInstId) {
        mmap.put("action", "viewProcessingImg");
        mmap.put("procInstId", procInstId);
        return "camunda/modeler-view";
    }

    @GetMapping("/getHistoryView")
    @ResponseBody
    public AjaxResult getHistoryView(String procInstId) {
        return success(processService.getHistoryView(procInstId));
    }

    @PostMapping("/listHistory")
    @ResponseBody
    public TableDataInfo listHistory(HistoricActivityVO historicActivityVO) {
        List<HistoricActivityVO> list = processService.listHistory(historicActivityVO);
        return getDataTable(list);
    }

    @PostMapping("/updateHistoryTaskDescription")
    @ResponseBody
    public AjaxResult updateHistoryTaskDescription(@RequestParam Map<String, Object> map) {
        return success(processService.updateHistoryTaskDescription(map));
    }
}
