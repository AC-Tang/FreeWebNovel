package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.BookRequests;
import com.example.freefiction.entity.Notifications;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.BookRequestsService;
import com.example.freefiction.mapper.BookRequestsMapper;
import com.example.freefiction.service.sys.NotificationsService;
import com.example.freefiction.service.sys.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
* @author Tjianwei
* @description 针对表【book_requests(书籍请求表)】的数据库操作Service实现
* @createDate 2025-11-24 10:49:18
*/
@Service
@RequiredArgsConstructor
public class BookRequestsServiceImpl extends ServiceImpl<BookRequestsMapper, BookRequests>
    implements BookRequestsService{
    private final UsersService usersService;
    private final NotificationsService notificationsService;

    @Override
    public ServiceResult<Boolean> addBookRequest(BookRequests bookRequests){
        bookRequests.setCreatedAt(new Date());
        bookRequests.setStatus(1);
        boolean result = this.saveOrUpdate(bookRequests);
        if(!result){
            return ServiceResult.fail("添加书籍请求失败");
        }
        return ServiceResult.ok(true);
    }

    @Override
    public ServiceResult<Boolean> removeBookRequest(Long  id){
        boolean result = this.remove(new QueryWrapper<BookRequests>().eq("id", id));
        if(!result){
            return ServiceResult.fail("删除书籍请求失败");
        }
        return ServiceResult.ok(true);
    }

    @Override
    public ServiceResult<Boolean> updateBookRequest(BookRequests bookRequest){
        Notifications notifications = new Notifications();
        notifications.setUserId(bookRequest.getUserId());
        notifications.setType(4);
        notifications.setTitle("书籍请求审核结果");
        notifications.setContent("您的书籍请求审核结果为：" + (bookRequest.getStatus() == 2 ? "通过" : "未通过")+"。"+bookRequest.getAdminNote());
        notifications.setRelatedId(bookRequest.getId());
        notifications.setRelatedType("book_requests");
        return this.update(new UpdateWrapper<BookRequests>().eq("id", bookRequest.getId()).set("status", bookRequest.getStatus()).set("admin_note", bookRequest.getAdminNote()).set("book_id", bookRequest.getBookId()))&&
                notificationsService.save( notifications)?ServiceResult.ok(true):ServiceResult.fail("更新书籍请求状态失败");
    }

    /**
     * 获取所有书籍请求
     * @return 书籍请求列表
     */
    @Override
    public ServiceResult<List<BookRequests>> getAll(){
        List<BookRequests> bookRequests = this.list();
        bookRequests.forEach(bookRequest -> {
            bookRequest.setUserName(usersService.getById(bookRequest.getUserId()).getUsername());
        });
        return ServiceResult.ok(bookRequests);
    }
}




