package cn.xilio.joty.adapter.portal.controller;


import cn.xilio.joty.adapter.portal.dto.request.*;
import cn.xilio.joty.service.ShortUrlService;
import cn.xilio.joty.core.common.page.PageQuery;
import cn.xilio.joty.core.common.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/short-url")
@Tag(name = "短链接口")
public class PortalShortUrlController {
    @Autowired
    private ShortUrlService shortUrlService;

    @Operation(summary = "创建单个短链接")
    @PostMapping(value = "create", name = "Create a single")
    public Result createSingleShortUrl(@Validated @RequestBody CreateSingleShortUrlRequest request) {
        return Result.success(shortUrlService.createSingle(request));
    }
    @Operation(summary = "快速创建短链接")
    @PostMapping(value = "fast-create", name = "Fast create a short url")
    public Result fastCreate(@Validated @RequestBody FastCreateShortUrlRequest request) {
        return Result.success(shortUrlService.fastCreateSingle(request));
    }

    @Operation(summary = "还原短链接")
    @PostMapping(value = "restore-url", name = "Restore short links")
    public Result restoreShortUrl(@Validated @RequestBody RestoreShortUrlRequest request) {
        return Result.success(shortUrlService.restoreShortUrl(request));
    }

    @Operation(summary = "批量创建短链接")
    @PostMapping(value = "create-batch", name = "Batch creation")
    public Result createBatchShortUrl(@Validated @RequestBody CreateBatchShortUrlRequest request) {
        return Result.success(shortUrlService.createBatchShortUrl(request));
    }
    @Operation(summary = "更新短链接")
    @PutMapping(value = "update", name = "Update short links")
    public Result updateShortUrl(@Validated @RequestBody UpdateShortUrlRequest request) {
        shortUrlService.update(request);
        return Result.success();
    }

    @Operation(summary = "短链接列表")
    @PostMapping(value = "list", name = "Short link pagination list")
    public Result getShortUrls(@Validated @RequestBody ShortUrlPageRequest request) {
        return Result.success(shortUrlService.getShortUrlsByUser(request));
    }

    @Operation(summary = "批量删除短链接")
    @DeleteMapping(value = "del", name = "Batch delete short links")
    public Result deleteShortUrls(@RequestBody List<String> ids) {
        shortUrlService.deleteShortUrl(ids);
        return Result.success();
    }

    @Operation(summary = "获取短链接信息")
    @GetMapping(value = "get", name = "Get short link information")
    public Result getShortUrlInfo(@RequestParam String id) {
        return Result.success(shortUrlService.getShortUrlInfo(id));
    }
}
