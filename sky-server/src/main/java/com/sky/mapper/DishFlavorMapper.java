package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品id删除口味数据
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);

    /**
     * 根据菜品id查询口味数据
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);

    /**
     * 更新口味数据
     * @param dishFlavor
     */
    void update(DishFlavor dishFlavor);

    /**
     * 根据ids批量删除口味数据
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
