package com.msb.rentcarhou.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.rentcarhou.common.result.Result;
import com.msb.rentcarhou.dto.LoginReqDto;
import com.msb.rentcarhou.dto.RegisterReqDto;
import com.msb.rentcarhou.dto.UserQueryDto;
import com.msb.rentcarhou.entity.SysUser;
import com.msb.rentcarhou.service.SysUserService;
import com.msb.rentcarhou.vo.LoginResVo;
import com.msb.rentcarhou.vo.UserInfoVo;
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

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterReqDto reqDto) {
        try {
            sysUserService.register(reqDto);
            return Result.success("注册成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<UserInfoVo> info() {
        try {
            UserInfoVo userInfoVo = sysUserService.getCurrentUserInfo();
            return Result.success(userInfoVo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<Page<SysUser>> getUserList(UserQueryDto queryDto) {
        try {
            Page<SysUser> page = sysUserService.getUserList(queryDto);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
