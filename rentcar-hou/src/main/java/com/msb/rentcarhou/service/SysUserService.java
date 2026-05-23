package com.msb.rentcarhou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.rentcarhou.dto.LoginReqDto;
import com.msb.rentcarhou.entity.SysUser;
import com.msb.rentcarhou.vo.LoginResVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.rentcarhou.dto.RegisterReqDto;
import com.msb.rentcarhou.dto.UserQueryDto;

public interface SysUserService extends IService<SysUser> {
    LoginResVo login(LoginReqDto reqDto);
    void register(RegisterReqDto reqDto);
    Page<SysUser> getUserList(UserQueryDto queryDto);
}
