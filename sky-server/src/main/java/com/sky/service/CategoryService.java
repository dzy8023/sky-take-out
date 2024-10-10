package com.sky.service;


import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.ArrayList;

public interface CategoryService {
    /**
     * 添加分类
     * @param categoryDTO
     */
    void addCategory(CategoryDTO categoryDTO);

    /**
     * 修改分类
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 删除分类
     * @param id
     */
    void delete(Long id);

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 启用或禁用分类
     * @param id
     * @param status
     */
    void startOrStop(Long id, Integer status);

    /**
     * 根据分类类型查询
     * @param type
     * @return
     */
    ArrayList<Category> queryByType(Integer type);
}
