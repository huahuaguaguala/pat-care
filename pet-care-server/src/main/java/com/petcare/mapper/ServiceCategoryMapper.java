package com.petcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.entity.ServiceCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServiceCategoryMapper extends BaseMapper<ServiceCategory> {
}
