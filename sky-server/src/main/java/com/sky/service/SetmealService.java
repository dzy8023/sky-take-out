package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    /**
     * 新增菜品
     * @param setmealDTO
     */
    void saveWithSetmealDish(SetmealDTO setmealDTO);


    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据id查询套餐和的对应套餐菜品信息
     *
     * @param id
     * @return
     */
    SetmealVO getByIdWithSetmealDish(Long id);

    /**
     * 批量删除套餐
     *
     * @param ids
     * @return
     */
    void deleteBatch(List<Long> ids);

    /**
     * 修改套餐和对应套餐菜品
     *
     * @param setmealDTO
     * @return
     */
    void updateWithSetmealDish(SetmealDTO setmealDTO);

    /**
     * 套餐起售或停售
     *
     * @param id
     * @param status
     * @return
     */
    void startOrStop(Long id, Integer status);
    /**
     * 根据分类id查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询包含的菜品列表
     * @param setmealId
     * @return
     */
    List<DishItemVO> getDishsById(Long setmealId);
}
