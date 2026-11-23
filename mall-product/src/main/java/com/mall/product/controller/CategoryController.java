package com.mall.product.controller;

import com.mall.common.result.Result;
import com.mall.product.entity.Category;
import com.mall.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品类目接口。
 *
 * @author 彭桂润
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 查询类目列表。
     */
    @GetMapping
    public Result<List<Category>> list(@RequestParam(value = "status", required = false) Integer status) {
        return Result.success(categoryService.listAll(status));
    }

    /**
     * 新增类目（管理员）。
     */
    @PostMapping
    public Result<Long> create(@RequestBody Category category) {
        return Result.success(categoryService.createCategory(category));
    }

    /**
     * 修改类目（管理员）。
     */
    @PutMapping("/{categoryId}")
    public Result<Void> update(@PathVariable("categoryId") Long categoryId, @RequestBody Category category) {
        categoryService.updateCategory(categoryId, category);
        return Result.success();
    }

    /**
     * 删除类目（管理员）。
     */
    @DeleteMapping("/{categoryId}")
    public Result<Void> remove(@PathVariable("categoryId") Long categoryId) {
        categoryService.removeCategory(categoryId);
        return Result.success();
    }
}
