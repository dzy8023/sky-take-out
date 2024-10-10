package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品和对应口味
     *
     * @param dishDTO
     */
    //保证事务的一致性，要么都成功，要么都失败
    //还要开启注解方式的事务管理(已在启动类上开启了)
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //向菜品表插入一条数据
        dishMapper.insert(dish);

        //获取insert语句生成的主键id值(具体看DishMapper.xml)
        Long dishId = dish.getId();

        //向口味表插入n条数据
        //防止value值为空
        List<DishFlavor> flavors = dishDTO.getFlavors();
        Iterator<DishFlavor> iterator = flavors.iterator();
        while (iterator.hasNext()) {
            DishFlavor next = iterator.next();
            next.setDishId(dishId);
            if (next.getName().isEmpty()) {
                iterator.remove();
            }
        }
        if (!flavors.isEmpty()) {
            dishFlavorMapper.insertBatch(flavors);
        }

        //将新增菜品的分类id的redis缓存清除
        String key="dish_"+dish.getCategoryId();
        cleanCache(key);
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品批量删除
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        int size = ids.size();
        Iterator<Long> iterator = ids.iterator();
        while (iterator.hasNext()) {
            Long id = (Long) iterator.next();
            //判断当前菜品是否处于起售中
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //单个删除给提示
                if (size == 1) {
                    throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
                }
                //当前菜品处于起售中，不能删除
                iterator.remove();
                continue;
            }
            //判断是否被套餐关联
            Integer setmealIdCount = setmealDishMapper.getSetmealIdCountByDishId(id);
            if (setmealIdCount != 0) {
                //单个删除给提示
                if (size == 1) {
                    throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
                }
                //当前菜品处于起售中，不能删除
                iterator.remove();
            }
        }
        if (ids.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.Failed_TO_Delete_DISH);
        }
        //删除菜品表中的菜品数据
        dishMapper.deleteByIds(ids);
        //删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);

        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        cleanCache("dish_*");
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品信息
        Dish dish = dishMapper.getById(id);

        //根据菜品id查询对应的口味信息
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);

        //将查询到的数据封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 更新菜品信息和对应的口味信息
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //更新菜品表信息
        dishMapper.update(dish);

        //更新口味表中对应菜品口味信息
        //查找口味表中该菜品所有口味id
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(dish.getId());
        //更新+添加
        Iterator<DishFlavor> iterator = dishDTO.getFlavors().iterator();
        Iterator<DishFlavor> iterator1 = flavors.iterator();
        List<DishFlavor> dishFlavors = new ArrayList<>();
        while (iterator.hasNext()) {
            DishFlavor next = iterator.next();
            if (next.getName().isEmpty()) {
                iterator.remove();
                continue;
            }
            next.setDishId(dishDTO.getId());
            if (iterator1.hasNext()) {
                next.setId(iterator1.next().getId());
                dishFlavors.add(next);
                iterator1.remove();
                iterator.remove();
            }
        }
        if (!dishFlavors.isEmpty()) {
            for (DishFlavor dishFlavor : dishFlavors) {
                dishFlavorMapper.update(dishFlavor);
            }
        }
        if (!dishDTO.getFlavors().isEmpty()) {
            dishFlavorMapper.insertBatch(dishDTO.getFlavors());
        }
        if (!flavors.isEmpty()) {
            List<Long> ids = flavors.stream().map(DishFlavor::getId).collect(Collectors.toList());
            dishFlavorMapper.deleteBatch(ids);
        }

        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        cleanCache("dish_*");
    }

    /**
     * 菜品启用或禁用
     *
     * @param id
     * @param status
     */
    @Transactional
    @Override
    public void startOrStop(Long id, Integer status) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        if(status==StatusConstant.DISABLE){
            //包含菜品的套餐停售
            List<Long>setmealIds= setmealDishMapper.getIdsByDishId(id);
            if (setmealIds!=null&& !setmealIds.isEmpty()) {
                setmealMapper.updateStatusByIds(setmealIds, status);
            }
        }
        dishMapper.update(dish);

        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        cleanCache("dish_*");
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 根据分类id和起售状态查询菜品信息及对应的口味
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        //构造redis中的key，规则:dish_categoryid
        String key="dish_"+dish.getCategoryId();
        //查询redis中是否存在菜品数据
        List<DishVO> list=(List<DishVO>) redisTemplate.opsForValue().get(key);
        if(list!=null){
            //如果存在，直接返回，无需查询数据库
            return list;
        }

        list=new ArrayList<>();
        //查询分类id下所有起售菜品
        for (Dish d:dishMapper.list(dish)){
            DishVO dishVO=new DishVO();
            BeanUtils.copyProperties(d,dishVO);
            dishVO.setFlavors(dishFlavorMapper.getByDishId(d.getId()));
            list.add(dishVO);
        }
        //如果不存在，查询数据库，将查询到的数据放入redis中
        //如果查询到的数据为空，存入缓存要设置过期时间
        //防止缓存穿透，设置缓存时间为30秒
        if(list.isEmpty()){
            redisTemplate.opsForValue().set(key,list,300, TimeUnit.SECONDS);
        }else {
            redisTemplate.opsForValue().set(key,list);
        }
        return list;
    }

    /**
     * 清理缓存数据
     * @param pattern
     */

    private void cleanCache(String pattern){
        Set keys=redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

}

