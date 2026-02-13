package com.ruoyi.project.oa.qingjia.controller;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.oa.qingjia.domain.OaQingjia;
import com.ruoyi.project.oa.qingjia.service.IOaQingjiaService;
import com.ruoyi.project.tool.hutool.IdGeneratorSnowflake;

/**
 * 请假Controller
 * 
 * @author ruoyi
 * @date 2026-02-08
 */
@Controller
@RequestMapping("/oa/qingjia")
public class OaQingjiaController extends BaseController {
    private String prefix = "oa/qingjia";

    @Autowired
    private IOaQingjiaService bizService;
    @Autowired
    private IdGeneratorSnowflake idGeneratorSnowflake;

    @GetMapping("/processForm")
    public String processForm(ModelMap mmap, String processId, String taskId, String node,
            String action) {
        if (StringUtils.isEmpty(action)) {
            mmap.put("processId", String.valueOf(idGeneratorSnowflake.nextId()));
            mmap.put("action", "new");
            mmap.put("node", "n1");
        } else {
            OaQingjia beanInfo = bizService.selectOaQingjiaById(processId);

            mmap.put("processId", processId);
            mmap.put("procInstId", beanInfo.getProcInstId());
            mmap.put("taskId", taskId);
            mmap.put("action", action);
            mmap.put("node", node);
            mmap.put("beanInfo", beanInfo);
        }

        return prefix + "/processForm";
    }

    @Log(title = "processFormStart", businessType = BusinessType.INSERT)
    @PostMapping("/processFormStart")
    @ResponseBody
    public AjaxResult processFormStart(OaQingjia oaQingjia) {
        return toAjax(bizService.processFormStart(oaQingjia));
    }

    @Log(title = "handle", businessType = BusinessType.UPDATE)
    @PostMapping("/handle")
    @ResponseBody
    public AjaxResult handle(OaQingjia oaQingjia) {
        return toAjax(bizService.handle(oaQingjia));
    }

    @Log(title = "draft", businessType = BusinessType.UPDATE)
    @PostMapping("/draft")
    @ResponseBody
    public AjaxResult draft(OaQingjia oaQingjia) {
        return toAjax(bizService.draft(oaQingjia));
    }

    @Log(title = "processFormEdit", businessType = BusinessType.UPDATE)
    @PostMapping("/processFormEdit")
    @ResponseBody
    public AjaxResult processFormEdit(OaQingjia oaQingjia) {
        return toAjax(bizService.processFormEdit(oaQingjia));
    }

    @RequiresPermissions("oa:qingjia:view")
    @GetMapping()
    public String qingjia() {
        return prefix + "/qingjia";
    }

    /**
     * 查询请假列表
     */
    @RequiresPermissions("oa:qingjia:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(OaQingjia oaQingjia) {
        startPage();
        List<OaQingjia> list = bizService.selectOaQingjiaList(oaQingjia);
        return getDataTable(list);
    }

    /**
     * 导出请假列表
     */
    @RequiresPermissions("oa:qingjia:export")
    @Log(title = "请假", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(OaQingjia oaQingjia) {
        List<OaQingjia> list = bizService.selectOaQingjiaList(oaQingjia);
        ExcelUtil<OaQingjia> util = new ExcelUtil<OaQingjia>(OaQingjia.class);
        return util.exportExcel(list, "请假数据");
    }

    /**
     * 新增请假
     */
    @RequiresPermissions("oa:qingjia:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存请假
     */
    @RequiresPermissions("oa:qingjia:add")
    @Log(title = "请假", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(OaQingjia oaQingjia) {
        return toAjax(bizService.insertOaQingjia(oaQingjia));
    }

    /**
     * 修改请假
     */
    @RequiresPermissions("oa:qingjia:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, ModelMap mmap) {
        OaQingjia oaQingjia = bizService.selectOaQingjiaById(id);
        mmap.put("oaQingjia", oaQingjia);
        return prefix + "/edit";
    }

    /**
     * 修改保存请假
     */
    @RequiresPermissions("oa:qingjia:edit")
    @Log(title = "请假", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(OaQingjia oaQingjia) {
        return toAjax(bizService.updateOaQingjia(oaQingjia));
    }

    /**
     * 删除请假
     */
    @RequiresPermissions("oa:qingjia:remove")
    @Log(title = "请假", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(bizService.deleteOaQingjiaByIds(ids));
    }
}
