package com.platform.biz.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.biz.entity.Category;
import com.platform.biz.mapper.CategoryMapper;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/biz/category")
@RequiredArgsConstructor
@Tag(name = "商品分类", description = "商品分类查询接口")
public class CategoryController {

    private final CategoryMapper categoryMapper;

    @GetMapping("/list")
    @Operation(summary = "获取全部分类", description = "获取所有商品分类列表（按排序号升序）")
    public Result<List<Category>> list() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        return Result.success(categoryMapper.selectList(wrapper));
    }
}
