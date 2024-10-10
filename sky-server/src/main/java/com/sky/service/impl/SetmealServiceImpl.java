package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;

import com.sky.result.PageResult;
import com.sky.service.SetmealService;

import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
private DishMapper dishMapper;

    /**
     * 新增套餐和对应套餐菜品
     *
     * @param setmealDTO
     */
    @Transactional
    public void saveWithSetmealDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //向套餐表插入一条数据
        setmealMapper.insert(setmeal);

        //获取insert语句生成的主键id值(具体看SetmealMapper.xml)
        Long setmealId = setmeal.getId();

        //向套餐菜品表插入多条数据
        //防止setmealDTO.getSetmealDishes()为空
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(setmealId);
        }
        if (!setmealDishes.isEmpty()) {
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id查询套餐和的对应套餐菜品信息
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithSetmealDish(Long id) {
        //根据id查询套餐信息
        Setmeal setmeal = setmealMapper.getById(id);
        //根据菜品id查询套餐菜品信息
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        //将数据封装到VO
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 批量删除套餐
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        int size = ids.size();
        Iterator<Long> iterator = ids.iterator();
        while (iterator.hasNext()) {
            Long id = iterator.next();
            //根据id查询套餐信息
            Setmeal setmeal = setmealMapper.getById(id);
            //判断套餐是否售卖
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                //售卖中不能删除
                if (size == 1) {
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
                }
                iterator.remove();
            }
        }
        if (ids.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.Failed_TO_Delete_DISH);
        }
        //批量删除套餐
        setmealMapper.deleteByIds(ids);
        //批量删除套餐菜品
        setmealDishMapper.deleteBySetmealIds(ids);
    }
 
    /**
     * 修改套餐和对应套餐菜品
     * TODO锁表
     *
     * @param setmealDTO
     * @return
     */
    @Override
    @Transactional
    public void updateWithSetmealDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //更新套餐表信息
        setmealMapper.update(setmeal);
        //更新套餐菜品表信息
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(setmealDTO.getId());
        Iterator<SetmealDish> iterator = setmealDTO.getSetmealDishes().iterator();
        Iterator<SetmealDish> iterator1 = setmealDishes.iterator();
        List<SetmealDish> setmealDishList = new ArrayList<>();
        while (iterator.hasNext()) {
            SetmealDish next = iterator.next();
            next.setSetmealId(setmealDTO.getId());
            if (iterator1.hasNext()) {
                next.setId(iterator1.next().getId());
                setmealDishList.add(next);
                iterator1.remove();
                iterator.remove();
            }
        }
        if (!setmealDishList.isEmpty()) {
            for (SetmealDish setmealDish : setmealDishList) {
                setmealDishMapper.update(setmealDish);
            }
        }
        if (!setmealDTO.getSetmealDishes().isEmpty()) {
            setmealDishMapper.insertBatch(setmealDTO.getSetmealDishes());
        }
        if (!setmealDishes.isEmpty()) {
            List<Long> ids = setmealDishes.stream().map(SetmealDish::getId).collect(Collectors.toList());
            setmealDishMapper.deleteBatch(ids);
        }
    }

    /**
     * 套餐起售或停售
     *
     * @param id
     * @param status
     * @return
     */
    @Override
    public void startOrStop(Long id, Integer status) {
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        if(status == StatusConstant.ENABLE){
           List<SetmealDish> setmealDish=setmealDishMapper.getBySetmealId(id);
            for (SetmealDish sdish : setmealDish) {
                if(dishMapper.getStatusById(sdish.getDishId())==StatusConstant.DISABLE){
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }
        setmealMapper.update(setmeal);
    }

    /**
     * 根据分类id查询套餐
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal>list=setmealMapper.list(setmeal);
        return list;
    }
    /**
     * 根据套餐id查询包含的菜品列表
     * @param setmealId
     * @return
     */
    @Override
    public List<DishItemVO> getDishsById(Long setmealId) {
        return setmealDishMapper.getDishsBySetmealID(setmealId);
    }
}
