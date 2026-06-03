package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Categories;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.CategoriesService;
import com.example.freefiction.mapper.CategoriesMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
* @author Tjianwei
* @description 针对表【categories(分类表)】的数据库操作Service实现
* @createDate 2025-11-24 10:49:10
*/
@Service
public class CategoriesServiceImpl extends ServiceImpl<CategoriesMapper, Categories> implements CategoriesService{

    /**
     * 获取所有分类
     * @return 分类列表
     */
    @Override
    public List<Categories> getAllCategories() {
        return this.list();
    }

    /**
     * 创建或获取分类（如果分类不存在则创建）
     * @param name 分类名称
     * @return 分类对象
     */
    @Override
    public Categories createOrGetCategory(String name) {
        // 查询分类是否存在
        Categories category = this.getOne(new QueryWrapper<Categories>().eq("name", name));

        if (category != null) {
            // 分类已存在，返回分类对象
            return category;
        } else {
            // 分类不存在，创建新分类
            Categories newCategory = new Categories();
            newCategory.setName(name);
            newCategory.setIcon("");
            // 设置sort_order为当前最大排序值+1，确保新分类排在最后
            int maxSortOrder = this.list().stream()
                    .mapToInt(Categories::getSortOrder)
                    .max()
                    .orElse(0);
            newCategory.setSortOrder(maxSortOrder + 1);
            newCategory.setStatus(1);
            newCategory.setCreatedAt(new Date());
            newCategory.setUpdatedAt(new Date());

            this.save(newCategory);
            return newCategory;
        }
    }

    /**
     * 添加分类
     * @param categories 分类对象
     * @return 添加结果
     */
    @Override
    public ServiceResult<Boolean> addCategory(Categories categories){
        return this.save(categories) ? ServiceResult.ok(true) : ServiceResult.fail("添加失败");
    }

    /**
     * 删除分类
     * @param categoryId 分类ID
     * @return 删除结果
     */
    @Override
    public ServiceResult<Boolean> deleteCategory(Long categoryId){
        return this.removeById(categoryId) ? ServiceResult.ok(true) : ServiceResult.fail("删除失败");
    }
}




