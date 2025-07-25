package cn.xilio.joty.adapter.admin.controller;

import cn.xilio.joty.core.common.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class AdminDashboardController {

    @GetMapping(value = "stats-count",name = "Get stats count")
    public Result getStatsCount() {
        return Result.success();
    }
}
