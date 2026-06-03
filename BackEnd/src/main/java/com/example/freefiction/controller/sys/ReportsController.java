package com.example.freefiction.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.freefiction.entity.Reports;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {
    private final ReportsService reportsService;

    /**
     * 创建举报
     * @param reports 举报信息
     * @return 是否成功
     */
    @PostMapping("/create")
    public Result<Boolean> createReport(@RequestBody Reports reports) {
        ServiceResult<Boolean> result = reportsService.createReport(reports);
        return result.success()?
                Result.success("提交举报成功", result.data()):
                Result.failed(500,"提交举报失败");
    }

    /**
     * 获取所有举报
     * @param status 状态
     * @return 所有举报
     */
    @GetMapping("/fetch/all")
    public Result<List<Reports>> getReportsByStatus(@RequestParam Integer status) {
        ServiceResult<List<Reports>> result = reportsService.getReportsByStatus(status);
        return result.success()?
                Result.success("获取举报成功", result.data()):
                Result.failed(500,"获取举报失败");
    }

    /**
     * 更新举报
     * @param reports 举报对象
     * @return 是否成功
     */
    @PutMapping("/update")
    public Result<Boolean> updateReportStatus(@RequestBody Reports  reports) {
        ServiceResult<Boolean> result = reportsService.updateReport( reports);
        return result.success()?
                Result.success("更新举报状态成功", result.data()):
                Result.failed(500,"更新举报状态失败");
    }

    /**
     * 删除举报
     * @param reportId 举报id
     * @return 是否成功
     */
    @DeleteMapping("/delete")
    public Result<Boolean> deleteReport(@RequestParam Long reportId) {
        ServiceResult<Boolean> result = reportsService.deleteReport(reportId);
        return result.success()?
                Result.success("删除举报成功", result.data()):
                Result.failed(500,"删除举报失败");
    }

    /**
     * 获取举报数量
     * @param status 状态
     * @return 举报数量
     */
    @GetMapping("/count")
    public Result<Long> count(@RequestParam Integer status) {
        return Result.success("获取举报数量成功", reportsService.count(new QueryWrapper<Reports>().eq("status", status)));
    }

    /**
     * 获取所有举报
     * @return 所有举报
     */
    @GetMapping("/all")
    public Result<List<Reports>> getAll() {
        return reportsService.getAll().success()?
                Result.success("获取所有举报成功", reportsService.getAll().data()):
                Result.failed(500,"获取所有举报失败");
    }
}
