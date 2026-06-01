package com.petcare.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.entity.PetBreed;
import com.petcare.mapper.PetBreedMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/breed")
public class BreedController {
    @Autowired private PetBreedMapper breedMapper;

    @GetMapping
    public Result<?> list(@RequestParam(required = false) String category) {
        LambdaQueryWrapper<PetBreed> qw = new LambdaQueryWrapper<PetBreed>()
                .eq(PetBreed::getStatus, 1)
                .orderByAsc(PetBreed::getSort);
        if (category != null) qw.eq(PetBreed::getCategory, category);
        return Result.success(breedMapper.selectList(qw));
    }

    @PostMapping
    public Result<?> add(@RequestBody PetBreed breed, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "Admin only");
        breedMapper.insert(breed);
        return Result.success(breed);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody PetBreed breed, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "Admin only");
        breed.setId(id);
        breedMapper.updateById(breed);
        return Result.success(breedMapper.selectById(id));
    }
}
