package com.petcare.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.entity.Pet;
import com.petcare.mapper.PetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/pet")
public class PetController {
    @Autowired private PetMapper petMapper;

    @PostMapping
    public Result<?> add(@RequestBody Pet pet, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        pet.setOwnerId(userId);
        pet.setPopularity(0);
        petMapper.insert(pet);
        // 自动生成店内编号: P-{id}
        pet.setStoreNo("P-" + String.format("%04d", pet.getId()));
        petMapper.updateById(pet);
        return Result.success(petMapper.selectById(pet.getId()));
    }

    @GetMapping("/my")
    public Result<?> myPets(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        List<Pet> pets = petMapper.selectList(
                new LambdaQueryWrapper<Pet>().eq(Pet::getOwnerId, userId));
        return Result.success(pets);
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) return Result.fail("宠物不存在");
        return Result.success(pet);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Pet pet, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Pet exist = petMapper.selectById(id);
        if (exist == null || !exist.getOwnerId().equals(userId)) {
            return Result.fail("无权操作");
        }
        pet.setId(id);
        pet.setOwnerId(null);
        petMapper.updateById(pet);
        return Result.success(petMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Pet exist = petMapper.selectById(id);
        if (exist == null || !exist.getOwnerId().equals(userId)) {
            return Result.fail("无权操作");
        }
        petMapper.deleteById(id);
        return Result.success();
    }
}
