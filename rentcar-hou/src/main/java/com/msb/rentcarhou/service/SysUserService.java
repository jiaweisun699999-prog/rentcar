package com.msb.rentcarhou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.rentcarhou.dto.LoginReqDto;
import com.msb.rentcarhou.entity.SysUser;
import com.msb.rentcarhou.vo.LoginResVo;

public interface SysUserService extends IService<SysUser> {
    LoginResVo login(LoginReqDto reqDto);
}
