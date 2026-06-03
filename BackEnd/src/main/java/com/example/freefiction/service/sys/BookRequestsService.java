package com.example.freefiction.service.sys;

import com.example.freefiction.entity.BookRequests;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【book_requests(书籍请求表)】的数据库操作Service
* @createDate 2025-11-24 10:49:18
*/
@Service
public interface BookRequestsService extends IService<BookRequests> {
    /**
     * 添加书籍请求
     * @param bookRequests 书籍请求信息
     * @return 是否添加成功
     */
    ServiceResult<Boolean> addBookRequest(BookRequests bookRequests);

    /**
     * 移除书籍请求
     * @param bookRequestId 书籍请求ID
     * @return 是否移除成功
     */
    ServiceResult<Boolean> removeBookRequest(Long bookRequestId);

    /**
     * 修改书籍请求
     * @param bookRequests 书籍请求信息
     * @return 是否修改成功
     */
    ServiceResult<Boolean> updateBookRequest(BookRequests bookRequests);

    /**
     * 获取所有书籍请求
     * @return 书籍请求列表
     */
    ServiceResult<List<BookRequests>> getAll();
}
