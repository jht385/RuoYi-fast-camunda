package com.ruoyi.project.oa.qingjia.domain;

import java.util.Date;

import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 请假对象 oa_qingjia
 * 
 * @author ruoyi
 * @date 2026-02-08
 */
@Getter
@Setter
public class OaQingjia extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** id */
    private String id;

    /** 流程实例id */
    @Excel(name = "流程实例id")
    private String procInstId;

    private String status;

    /** 名称 */
    @Excel(name = "名称")
    private String name;

    /** 天数 */
    @Excel(name = "天数")
    private Long day;

    private Date quitDate;

    /** 原因 */
    @Excel(name = "原因")
    private String reason;

    //
    private String processId;
    private String taskId;
    private String node;
    private Integer pass;
    private String nextMsg;
    private Long[] nextDept;
    private Long[] nextUser;
}
