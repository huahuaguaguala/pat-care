package com.petcare.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.entity.Order;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface OrderMapper extends BaseMapper<Order> {}
