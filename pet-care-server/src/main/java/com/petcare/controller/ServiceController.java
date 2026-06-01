package com.petcare.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.entity.ServiceCategory;
import com.petcare.entity.ServiceItem;
import com.petcare.mapper.ServiceCategoryMapper;
import com.petcare.mapper.ServiceItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/service")
public class ServiceController {
    @Autowired private ServiceCategoryMapper categoryMapper;
    @Autowired private ServiceItemMapper itemMapper;

    @GetMapping("/category")
    public Result<?> categories() {
        List<ServiceCategory> list = categoryMapper.selectList(
                new LambdaQueryWrapper<ServiceCategory>()
                    .eq(ServiceCategory::getStatus, 1)
                    .orderByAsc(ServiceCategory::getSort));
        return Result.success(list);
    }

    @PostMapping("/category")
    public Result<?> addCategory(@RequestBody ServiceCategory cat, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "仅店长可操作");
        categoryMapper.insert(cat);
        return Result.success(cat);
    }

    @GetMapping("/item")
    public Result<?> items(@RequestParam(required = false) Long categoryId) {
        LambdaQueryWrapper<ServiceItem> qw = new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getStatus, 1);
        if (categoryId != null) qw.eq(ServiceItem::getCategoryId, categoryId);
        return Result.success(itemMapper.selectList(qw));
    }

    @GetMapping("/item/{id}")
    public Result<?> itemDetail(@PathVariable Long id) {
        return Result.success(itemMapper.selectById(id));
    }

    @PostMapping("/item")
    public Result<?> addItem(@RequestBody ServiceItem item, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "仅店长可操作");
        itemMapper.insert(item);
        return Result.success(item);
    }
}
