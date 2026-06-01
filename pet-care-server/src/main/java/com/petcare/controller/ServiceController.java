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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/service")
public class ServiceController {
    @Autowired private ServiceCategoryMapper categoryMapper;
    @Autowired private ServiceItemMapper itemMapper;

    // === Categories (public read, admin write) ===

    @GetMapping("/category")
    public Result<?> categories() {
        return Result.success(categoryMapper.selectList(
                new LambdaQueryWrapper<ServiceCategory>()
                        .eq(ServiceCategory::getStatus, 1)
                        .orderByAsc(ServiceCategory::getSort)));
    }

    @GetMapping("/category/all")
    public Result<?> allCategories(HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "Admin only");
        return Result.success(categoryMapper.selectList(null));
    }

    @PostMapping("/category")
    public Result<?> addCategory(@RequestBody ServiceCategory cat, HttpServletRequest req) {
        if ((Integer) req.getAttribute("role") != 2) return Result.fail(403, "Admin only");
        categoryMapper.insert(cat);
        return Result.success(cat);
    }

    @PutMapping("/category/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody ServiceCategory cat, HttpServletRequest req) {
        if ((Integer) req.getAttribute("role") != 2) return Result.fail(403, "Admin only");
        cat.setId(id);
        categoryMapper.updateById(cat);
        return Result.success(categoryMapper.selectById(id));
    }

    @DeleteMapping("/category/{id}")
    public Result<?> deleteCategory(@PathVariable Long id, HttpServletRequest req) {
        if ((Integer) req.getAttribute("role") != 2) return Result.fail(403, "Admin only");
        categoryMapper.deleteById(id);
        return Result.success();
    }

    // === Service items (public read, staff+admin write) ===

    @GetMapping("/item")
    public Result<?> items(@RequestParam(required = false) Long categoryId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<ServiceItem> qw = new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getStatus, 1)
                // Only show services whose date range is active (start <= today, end is null or >= today)
                .and(w -> w.isNull(ServiceItem::getStartDate).or().le(ServiceItem::getStartDate, today))
                .and(w -> w.isNull(ServiceItem::getEndDate).or().ge(ServiceItem::getEndDate, today));
        if (categoryId != null) qw.eq(ServiceItem::getCategoryId, categoryId);
        return Result.success(itemMapper.selectList(qw));
    }

    @GetMapping("/item/all")
    public Result<?> allItems(HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1 && role != 2) return Result.fail(403, "Staff/admin only");
        return Result.success(itemMapper.selectList(null));
    }

    @GetMapping("/item/{id}")
    public Result<?> itemDetail(@PathVariable Long id) {
        return Result.success(itemMapper.selectById(id));
    }

    @PostMapping("/item")
    public Result<?> addItem(@RequestBody ServiceItem item, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2 && role != 1) return Result.fail(403, "Staff/admin only");
        itemMapper.insert(item);
        return Result.success(item);
    }

    @PutMapping("/item/{id}")
    public Result<?> updateItem(@PathVariable Long id, @RequestBody ServiceItem item, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2 && role != 1) return Result.fail(403, "Staff/admin only");
        item.setId(id);
        itemMapper.updateById(item);
        return Result.success(itemMapper.selectById(id));
    }

    @DeleteMapping("/item/{id}")
    public Result<?> deleteItem(@PathVariable Long id, HttpServletRequest req) {
        if ((Integer) req.getAttribute("role") != 2) return Result.fail(403, "Admin only");
        itemMapper.deleteById(id);
        return Result.success();
    }

    /** Toggle service on/off (staff can enable seasonal services, disable regular ones) */
    @PutMapping("/item/{id}/toggle")
    public Result<?> toggleItem(@PathVariable Long id, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2 && role != 1) return Result.fail(403, "Staff/admin only");
        ServiceItem item = itemMapper.selectById(id);
        if (item == null) return Result.fail("Service not found");
        item.setStatus(item.getStatus() == 1 ? 0 : 1);
        itemMapper.updateById(item);
        return Result.success(item);
    }

    /** Set time range for a service (e.g., Christmas grooming from Dec 20 to Dec 31) */
    @PutMapping("/item/{id}/timerange")
    public Result<?> setTimeRange(@PathVariable Long id, @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2 && role != 1) return Result.fail(403, "Staff/admin only");
        ServiceItem item = itemMapper.selectById(id);
        if (item == null) return Result.fail("Service not found");
        item.setStartDate(startDate != null ? LocalDate.parse(startDate) : null);
        item.setEndDate(endDate != null ? LocalDate.parse(endDate) : null);
        itemMapper.updateById(item);
        return Result.success(item);
    }
}
