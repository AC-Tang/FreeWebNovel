package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.Books;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.BooksService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BooksController {
    private final BooksService booksService;

    /**
     * 获取小说
     * @param bookId 小说ID
     * @return 一本小说
     */
    @GetMapping("/fetch/{bookId}")
    public Result<Books> fetchBook(@PathVariable Long bookId) {
        ServiceResult<Books> serviceResult = booksService.getBooksById(bookId);
        return serviceResult.success() ? Result.success("获取小说成功",serviceResult.data()):
                Result.failed(404,"小说资源不存在");
    }

    /**
     * 获取小说列表
     * @return 小说列表
     */
    @GetMapping("/fetch/all")
    public Result<List<Books>> fetchBookList() {
        ServiceResult<List<Books>> serviceResult = booksService.getAll();
        return serviceResult.success() ? Result.success("获取小说列表成功",serviceResult.data()):
                Result.failed(404,"小说列表不存在或数据库资源为空");
    }

    /**
     * 根据完本状态获取小说列表
     * @param status 状态
     * @return 小说列表
     */
    @GetMapping("/fetch/status")
    public Result<List<Books>> fetchBookListByStatus(@RequestParam Integer status) {
        ServiceResult<List<Books>> serviceResult = booksService.getBooksByStatus(status);
        return serviceResult.success() ? Result.success("获取小说列表成功",serviceResult.data()):
                Result.failed(404,"小说列表不存在或数据库资源为空");
    }

    /**
     * 根据分类获取小说列表
     * @param categoryId 分类ID
     * @return 小说列表
     */
    @GetMapping("/fetch/Category")
    public Result<List<Books>> fetchBookListByCategory(@RequestParam Integer  categoryId) {
        ServiceResult<List<Books>> serviceResult = booksService.getBooksByCategory(categoryId);
        return serviceResult.success() ? Result.success("获取小说列表成功",serviceResult.data()):
                Result.failed(404,"小说列表不存在或数据库资源为空");
    }


    /**
     * 增加小说浏览量
     * @param bookId 小说ID
     * @return 增加结果
     */
    @PostMapping("/add/viewCount")
    public Result<Boolean> addViewCount(@RequestParam Long bookId) {
        ServiceResult<Boolean> serviceResult = booksService.addViewCount(bookId);
        return serviceResult.success() ? Result.success("增加小说浏览量成功",serviceResult.data()):
                Result.failed(500,"增加小说浏览量失败");
    }

    /**
     * 增加小说
     * @param books 小说
     * @return 增加结果
     */
    @PostMapping("/add")
    public Result<Boolean> addBook(@RequestBody Books books) {
        ServiceResult<Boolean> serviceResult = booksService.addBooks(books);
        return serviceResult.success() ? Result.success("增加小说成功",serviceResult.data()):
                Result.failed(500,"增加小说失败");
    }

    /**
     * 更新小说
     * @param books 小说
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<Boolean> updateBook(@RequestBody Books books) {
        ServiceResult<Boolean> serviceResult = booksService.updateBooks(books);
        return serviceResult.success() ? Result.success("更新小说成功",serviceResult.data()):
                Result.failed(500,"更新小说失败");
    }

    /**
     * 删除小说
     * @param bookId 小说ID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<Boolean> deleteBook(@RequestParam Long bookId) {
        ServiceResult<Boolean> serviceResult = booksService.deleteBooks(bookId);
        return serviceResult.success() ? Result.success("删除小说成功",serviceResult.data()):
                Result.failed(500,"删除小说失败");
    }

    /**
     * 批量更新小说状态
     * @param bookIds 小说ID列表
     * @param status 状态
     * @return 批量更新结果
     */
    @PutMapping("/batchUpdate/status")
    public Result<Boolean> batchUpdateStatus(@RequestParam List<Long> bookIds,@RequestParam Integer status) {
        ServiceResult<Boolean> serviceResult = booksService.batchUpdateStatus(bookIds,status);
        return serviceResult.success() ? Result.success("批量更新小说状态成功",serviceResult.data()):
                Result.failed(500,"批量更新小说状态失败");
    }

    /**
     * 批量删除小说
     * @param bookIds 小说ID列表
     * @return 批量删除结果
     */
    @DeleteMapping("/batchDelete")
    public Result<Boolean> deleteAll(@RequestParam List<Long> bookIds) {
        ServiceResult<Boolean> serviceResult = booksService.batchDelete(bookIds);
        return serviceResult.success() ? Result.success("批量删除小说成功",serviceResult.data()):
                Result.failed(500,"批量删除小说失败");
    }

    /**
     * 获取小说数量
     * @return 小说数量
     */
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success("获取小说数量成功",booksService.count());
    }
}