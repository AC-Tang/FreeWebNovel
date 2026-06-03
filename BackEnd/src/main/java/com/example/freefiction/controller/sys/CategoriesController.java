package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.Categories;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.CategoriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoriesController {
    private final CategoriesService categoriesService;

    /**
     * 获取所有分类
     * @return 所有分类
     */
    @GetMapping("/fetch/all")
    public Result<List<Categories>> getAllCategories() {
        return Result.success(categoriesService.getAllCategories());
    }

    /**
     * 添加分类
     * @param categories 分类
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<Boolean> addCategory(@RequestBody Categories categories) {
        ServiceResult<Boolean> serviceResult = categoriesService.addCategory(categories);
        return serviceResult.success() ? Result.success("添加分类成功",serviceResult.data()) : Result.failed(404,"添加失败");
    }

    /**
     * 删除分类
     * @param categoryId 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<Boolean> deleteCategory(@RequestParam Long categoryId) {
        ServiceResult<Boolean> serviceResult = categoriesService.deleteCategory(categoryId);
        return serviceResult.success() ? Result.success("删除分类成功",serviceResult.data()) : Result.failed(404,"删除失败");
    }
}