package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询对应套餐id的数量
     * @param id
     * @return
     */
    @Select("select count(setmeal_id) from setmeal_dish where dish_id=id")
    Integer getSetmealIdCountByDishId(Long id);

    /**
     * 根据菜品id查询对应套餐id
     * @param id
     * @return
     */
    @Select("select setmeal_id from setmeal_dish where dish_id=#{id}")
    List<Long> getIdsByDishId(Long id);

    /**
     * 批量插入
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id查询对应的套餐菜品
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id=#{id}")
    List<SetmealDish> getBySetmealId(Long id);

    /**
     * 根据套餐id批量删除
     * @param ids
     */
    void deleteBySetmealIds(List<Long> ids);

    /**
     * 根据主键id更新菜品id，套餐菜品名称，套餐菜品价格，套餐菜品份数
     * @param setmealDish
     */
    @Update("update setmeal_dish set dish_id=#{dishId}," +
        " name=#{name},price=#{price},copies=#{copies} where id=#{id}")
    void update(SetmealDish setmealDish);

    /**
     * 根据主键id批量删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据套餐id查询菜品
     * @param setmealId
     * @return
     */
    @Select("select sd.name,sd.copies,d.image,d.description  from setmeal_dish sd left join dish d on sd.dish_id=d.id " +
            "where  sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishsBySetmealID(Long setmealId);
}
