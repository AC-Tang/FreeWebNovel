package com.example.freefiction.service.sys;

import com.example.freefiction.entity.Categories;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【categories(分类表)】的数据库操作Service
* @createDate 2025-11-24 10:49:10
*/
public interface CategoriesService extends IService<Categories> {
    /**
     * 获取所有分类
     * @return 分类列表
     */
    List<Categories> getAllCategories();


    /**
     * 创建或获取分类（如果分类不存在则创建）
     * @param name 分类名称
     * @return 分类对象
     */
    Categories createOrGetCategory(String name);

    /**
     * 添加分类
     * @param categories 分类对象
     * @return 添加结果
     */
    ServiceResult<Boolean> addCategory(Categories categories);

    /**
     * 删除分类
     * @param categoryId 分类ID
     * @return 删除结果
     */
    ServiceResult<Boolean> deleteCategory(Long categoryId);
}
