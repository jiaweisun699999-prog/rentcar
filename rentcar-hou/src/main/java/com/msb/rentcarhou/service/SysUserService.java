package com.msb.rentcarhou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.rentcarhou.dto.LoginReqDto;
import com.msb.rentcarhou.entity.SysUser;
import com.msb.rentcarhou.vo.LoginResVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.rentcarhou.dto.RegisterReqDto;
import com.msb.rentcarhou.dto.UserQueryDto;
import com.msb.rentcarhou.vo.UserInfoVo;

public interface SysUserService extends IService<SysUser> {
    LoginResVo login(LoginReqDto reqDto);
    void register(RegisterReqDto reqDto);
    UserInfoVo getCurrentUserInfo();
    Page<SysUser> getUserList(UserQueryDto queryDto);
    
    // 实名认证与信用相关
    com.msb.rentcarhou.vo.UserCreditInfoVo getCreditInfo();
    void certify(com.msb.rentcarhou.dto.CertifyReqDto reqDto);
}

