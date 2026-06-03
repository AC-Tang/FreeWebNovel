package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.Bookshelves;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.BookshelvesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/bookshelves")
public class BookshelvesController {
    private final BookshelvesService bookshelvesService;

    /**
     * 获取用户书架
     * @param userId 用户ID
     * @return 书架列表
     */
    @GetMapping("/all")
    public Result<List<Bookshelves>> getAllBookshelves(@RequestParam Long userId) {
        ServiceResult<List<Bookshelves>> serviceResult = bookshelvesService.getBookshelvesByUserId(userId);
        return serviceResult.success() ?
                Result.success("查询成功",serviceResult.data()):
                Result.failed(500,"查询失败");
    }

    /**
     * 判断书籍是否在书架中
     * @param userId 用户ID
     * @param novelId 书籍ID
     * @return 是否在书架中
     */
    @GetMapping("/exist")
    public Result<Boolean> exist(@RequestParam Long userId, @RequestParam Long novelId) {
        ServiceResult<Boolean> serviceResult = bookshelvesService.existInBookshelves(userId, novelId);
        return serviceResult.success() ?
                Result.success("查询成功",serviceResult.data()):
                Result.failed(500,"查询失败");
    }

    /**
     * 添加书籍到书架
     * @param bookshelves 书架对象
     * @return 是否添加成功
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Bookshelves bookshelves) {
        ServiceResult<Boolean> serviceResult = bookshelvesService.addBookToBookshelves(bookshelves);
        return serviceResult.success() ?
                Result.success("添加成功",serviceResult.data()):
                Result.failed(500,"添加失败");
    }

    /**
     * 删除书籍从书架
     * @param bookshelves 书架对象
     * @return 是否删除成功
     */
    @DeleteMapping("/remove")
    public Result<Boolean> remove(@RequestBody Bookshelves bookshelves) {
        ServiceResult<Boolean> serviceResult = bookshelvesService.removeBookFromBookshelves(bookshelves);
        return serviceResult.success() ?
                Result.success("删除成功",serviceResult.data()):
                Result.failed(500,"删除失败");
    }

    /**
     * 创建书架
     * @param userId 用户ID
     * @param name 书架名称
     * @return 是否创建成功
     */
    @PostMapping("/create")
    public Result<Boolean> create(@RequestParam Long userId, @RequestParam String name) {
        ServiceResult<Boolean> serviceResult = bookshelvesService.createBookshelves(userId, name);
        return serviceResult.success() ?
                Result.success("创建书架成功",serviceResult.data()):
                Result.failed(500,"创建书架失败");
    }
    /**
     * 获取书架名称
     * @param userId 用户ID
     * @return 书架名称列表
     */
    @GetMapping("/fetch")
    public Result<List<Bookshelves>> fetch(@RequestParam Long userId) {
        ServiceResult<List<Bookshelves>> serviceResult = bookshelvesService.getBookshelvesName(userId);
        return serviceResult.success() ?
                Result.success("获取书架名称成功",serviceResult.data()):
                Result.failed(500,"获取书架名称失败");
    }

    /**
     * 获取书架书籍
     * @param userId 用户ID
     * @param id 书架id
     * @return 书架书籍列表
     */
    @GetMapping("/fetch/{id}")
    public Result<List<Bookshelves>> fetch(@RequestParam Long userId, @PathVariable Long id) {
        ServiceResult<List<Bookshelves>> serviceResult = bookshelvesService.getBookshelvesByName(userId,id);
        return Result.success("获取书架书籍成功",serviceResult.data());
    }
    /**
     * 更新书架书籍信息
     * @param bookshelves 书架对象
     * @return 是否更新成功
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Bookshelves bookshelves) {
        ServiceResult<Boolean> serviceResult = bookshelvesService.updateBookshelves(bookshelves);
        return serviceResult.success() ?
                Result.success("更新书架书籍信息成功",serviceResult.data()):
                Result.failed(500,"更新书架书籍信息失败");
    }
    /**
     * 删除书架
     * @param id 书架id
     * @return 是否删除成功
     */
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        ServiceResult<Boolean> serviceResult = bookshelvesService.deleteBookshelves(id);
        return serviceResult.success() ?
                Result.success("删除书架成功",serviceResult.data()):
                Result.failed(500,"删除书架失败");
    }

    /**
     * 将书籍移动到书架
     * @param booksId 书籍ID列表
     * @param id 目标书架ID
     * @param userId 用户ID
     * @return 是否移动成功
     */
    @PostMapping("/move")
    public Result<Boolean> move(@RequestParam Long booksId, @RequestParam Long id, @RequestParam Long userId) {
        ServiceResult<Boolean> serviceResult = bookshelvesService.moveBookToBookshelves(booksId, id, userId);
        return serviceResult.success() ?
                Result.success("移动书籍成功",serviceResult.data()):
                Result.failed(500,"移动书籍失败");
    }
}
