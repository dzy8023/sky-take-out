package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface DishService {

    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品批量删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 更新菜品信息和对应的口味信息
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 菜品启用或禁用
     * @param id
     * @param status
     */
    void startOrStop(Long id, Integer status);

    /**
     * 根据分类id查询菜品列表
     * @param categoryId
     * @return
     */
    List<Dish> list(Long categoryId);

    /**
     * 根据分类id和起售状态查询菜品信息及对应的口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
