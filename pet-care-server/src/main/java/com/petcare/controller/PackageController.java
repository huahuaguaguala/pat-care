package com.petcare.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.entity.ServicePackage;
import com.petcare.entity.PackageItem;
import com.petcare.mapper.ServicePackageMapper;
import com.petcare.mapper.PackageItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/package")
public class PackageController {
    @Autowired private ServicePackageMapper packageMapper;
    @Autowired private PackageItemMapper itemMapper;

    @GetMapping
    public Result<?> list() {
        List<ServicePackage> pkgs = packageMapper.selectList(
                new LambdaQueryWrapper<ServicePackage>().eq(ServicePackage::getStatus, 1));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ServicePackage p : pkgs) {
            Map<String, Object> m = new HashMap<>();
            m.put("package", p);
            m.put("items", itemMapper.selectList(
                    new LambdaQueryWrapper<PackageItem>().eq(PackageItem::getPackageId, p.getId())));
            result.add(m);
        }
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        ServicePackage pkg = packageMapper.selectById(id);
        if (pkg == null) return Result.fail("Package not found");
        Map<String, Object> m = new HashMap<>();
        m.put("package", pkg);
        m.put("items", itemMapper.selectList(
                new LambdaQueryWrapper<PackageItem>().eq(PackageItem::getPackageId, id)));
        return Result.success(m);
    }

    @PostMapping
    public Result<?> add(@RequestBody ServicePackage pkg, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "Admin only");
        packageMapper.insert(pkg);
        return Result.success(pkg);
    }

    @PostMapping("/{id}/items")
    public Result<?> addItem(@PathVariable Long id, @RequestBody List<PackageItem> items, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "Admin only");
        for (PackageItem item : items) {
            item.setPackageId(id);
            itemMapper.insert(item);
        }
        return Result.success(items);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody ServicePackage pkg, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "Admin only");
        pkg.setId(id);
        packageMapper.updateById(pkg);
        return Result.success(pkg);
    }
}
