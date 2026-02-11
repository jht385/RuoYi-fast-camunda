package com.ruoyi.project.oa.qingjia.service;

import java.util.List;

import com.ruoyi.project.camunda.service.ICommonProcessService;
import com.ruoyi.project.oa.qingjia.domain.OaQingjia;

/**
 * 请假Service接口
 * 
 * @author ruoyi
 * @date 2026-02-08
 */
public interface IOaQingjiaService extends ICommonProcessService<OaQingjia>
{
    /**
     * 查询请假
     * 
     * @param id 请假主键
     * @return 请假
     */
    public OaQingjia selectOaQingjiaById(String id);

    /**
     * 查询请假列表
     * 
     * @param oaQingjia 请假
     * @return 请假集合
     */
    public List<OaQingjia> selectOaQingjiaList(OaQingjia oaQingjia);

    /**
     * 新增请假
     * 
     * @param oaQingjia 请假
     * @return 结果
     */
    public int insertOaQingjia(OaQingjia oaQingjia);

    /**
     * 修改请假
     * 
     * @param oaQingjia 请假
     * @return 结果
     */
    public int updateOaQingjia(OaQingjia oaQingjia);

    /**
     * 批量删除请假
     * 
     * @param ids 需要删除的请假主键集合
     * @return 结果
     */
    public int deleteOaQingjiaByIds(String ids);

    /**
     * 删除请假信息
     * 
     * @param id 请假主键
     * @return 结果
     */
    public int deleteOaQingjiaById(String id);
}
