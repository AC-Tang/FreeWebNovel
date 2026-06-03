package com.example.freefiction.service.sys;

import com.example.freefiction.entity.Reports;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【reports(举报表)】的数据库操作Service
* @createDate 2025-11-24 10:48:42
*/
@Service
public interface ReportsService extends IService<Reports> {
    /**
     * 创建举报
     * @param reports 举报对象
     * @return 是否成功
     */
    ServiceResult<Boolean> createReport(Reports reports);

    /**
     * 根据状态获取所有举报
     * @param status 状态
     * @return 举报列表
     */
    ServiceResult<List<Reports>> getReportsByStatus(Integer status);

    /**
     * 删除举报
     * @param reportId 举报id
     * @return 是否成功
     */
    ServiceResult<Boolean> deleteReport(Long reportId);

    /**
     * 获取所有举报
     * @return 所有举报
     */
    ServiceResult<List<Reports>> getAll();

    /**
     * 更新举报
     * @param reports 举报对象
     * @return 是否成功
     */
    ServiceResult<Boolean> updateReport(Reports reports);
}
