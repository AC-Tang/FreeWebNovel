package com.example.freefiction.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.freefiction.entity.BookRequests;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.BookRequestsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/bookRequests")
public class BookRequestController {
    private final BookRequestsService bookRequestsService;
    /**
     * 添加书籍请求
     * @param bookRequests 书籍请求对象
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<Boolean> addBookRequest(@RequestBody BookRequests bookRequests) {
        System.out.println("请求体"+bookRequests);
        ServiceResult<Boolean> serviceResult = bookRequestsService.addBookRequest(bookRequests);
        return serviceResult.success() ?
                Result.success("添加成功",serviceResult.data()):
                Result.failed(500,"添加失败");
    }
    /**
     * 删除书籍请求
     * @param id 书籍请求id
     * @return 删除结果
     */
    @DeleteMapping("/remove")
    public Result<Boolean> removeBookRequest(@RequestParam Long id) {
        ServiceResult<Boolean> serviceResult = bookRequestsService.removeBookRequest(id);
        return serviceResult.success() ?
                Result.success("删除成功",serviceResult.data()):
                Result.failed(500,"删除失败");
    }

    /**
     * 更新书籍请求
     * @param bookRequests 书籍请求对象
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<Boolean> updateBookRequest(@RequestBody BookRequests bookRequests) {
        ServiceResult<Boolean> serviceResult = bookRequestsService.updateBookRequest(bookRequests);
        return serviceResult.success() ?
                Result.success("更新成功",serviceResult.data()):
                Result.failed(500,"更新失败");
    }

    /**
     * 获取书籍请求数量
     * @param status 书籍请求状态
     * @return 数量
     */
    @GetMapping("/count")
    public Result<Long> countBookRequest(@RequestParam Long status) {
        return Result.success("获取数量成功",bookRequestsService.count(new QueryWrapper<BookRequests>().eq("status",status)));
    }

    /**
     * 获取所有书籍请求
     * @return 书籍请求列表
     */
    @GetMapping("/all")
    public Result<List<BookRequests>> getAllBookRequest() {
        ServiceResult<List<BookRequests>> serviceResult = bookRequestsService.getAll();
        return serviceResult.success() ?
                Result.success("获取成功",serviceResult.data()):
                Result.failed(500,"获取失败");
    }
}
