package com.msb.rentcarhou.controller;

import com.msb.rentcarhou.common.result.Result;
import com.msb.rentcarhou.dto.LoginReqDto;
import com.msb.rentcarhou.service.SysUserService;
import com.msb.rentcarhou.vo.LoginResVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin // 允许跨域，或者在前端使用代理
public class UserController {

    private final SysUserService sysUserService;

    @PostMapping("/login")
    public Result<LoginResVo> login(@RequestBody LoginReqDto reqDto) {
        try {
            LoginResVo resVo = sysUserService.login(reqDto);
            return Result.success(resVo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
