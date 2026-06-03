package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Notifications;
import com.example.freefiction.entity.Reports;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.*;
import com.example.freefiction.mapper.ReportsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
* @author Tjianwei
* @description 针对表【reports(举报表)】的数据库操作Service实现
* @createDate 2025-11-24 10:48:42
*/
@Service
@RequiredArgsConstructor
public class ReportsServiceImpl extends ServiceImpl<ReportsMapper, Reports>
    implements ReportsService{
    private final UsersService usersService;
    private final BooksService booksService;
    private final CommentsService commentsService;
    private final NotificationsService notificationsService;
    /**
     * 创建举报
     * @param reports 举报对象
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> createReport(Reports reports){
        reports.setStatus(1);
        return save( reports)?ServiceResult.ok(true):ServiceResult.fail("举报失败");
    }

    /**
     * 根据状态获取所有举报
     * @param status 状态
     * @return 举报列表
     */
    @Override
    public ServiceResult<List<Reports>> getReportsByStatus(Integer status){
        return list(new QueryWrapper<Reports>().eq("status", status))==null?ServiceResult.fail("获取举报失败"):ServiceResult.ok(list(new QueryWrapper<Reports>().eq("status", status)));
    }

    /**
     * 删除举报
     * @param id 举报id
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> deleteReport(Long id){
        return removeById(id)?ServiceResult.ok(true):ServiceResult.fail("删除举报失败");
    }

    /**
     * 获取所有举报
     * @return 所有举报
     */
    @Override
    public ServiceResult<List<Reports>> getAll(){
        List<Reports> reports = list();
        reports.forEach(report -> {
            if(report.getIsAnonymous()!=1)
                report.setReporterName(usersService.getById(report.getReporterId()).getUsername());
            if(report.getTargetType()==1) {
                report.setTargetName(booksService.getById(report.getTargetId()).getTitle());
            }else if(report.getTargetType()==2){
                report.setTargetName(commentsService.getById(report.getTargetId()).getContent());
            }else if(report.getTargetType()==3){
                report.setTargetName(usersService.getById(report.getTargetId()).getUsername());
            }
            if(report.getAdminId()!=null)
                report.setAdminName(usersService.getById(report.getAdminId()).getUsername());
        });
        return ServiceResult.ok(reports);
    }

    /**
     * 更新举报
     * @param reports 举报对象
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> updateReport(Reports reports){
        if(reports.getStatus() == 3) {
            Notifications notifications = new Notifications();
            notifications.setUserId(reports.getReporterId());
            notifications.setType(5);
            notifications.setTitle("举报处理结果");
            notifications.setContent(reports.getAdminNote());
            notifications.setRelatedId(reports.getId());
            notifications.setRelatedType("reports");
            notificationsService.saveOrUpdate(notifications);
        }
        return update(new UpdateWrapper<Reports>().eq("id", reports.getId()).set("status", reports.getStatus()).set("admin_note", reports.getAdminNote()).set("admin_id", reports.getAdminId()).set("processed_at",new Date()))
                ?ServiceResult.ok(true):ServiceResult.fail("更新反馈失败");
    }
}




