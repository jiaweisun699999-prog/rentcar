package com.msb.rentcarhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.rentcarhou.common.utils.JwtUtils;
import com.msb.rentcarhou.dto.LoginReqDto;
import com.msb.rentcarhou.entity.SysUser;
import com.msb.rentcarhou.mapper.SysUserMapper;
import com.msb.rentcarhou.service.SysUserService;
import com.msb.rentcarhou.vo.LoginResVo;
import com.msb.rentcarhou.vo.UserInfoVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final JwtUtils jwtUtils;

    @Override
    public LoginResVo login(LoginReqDto reqDto) {
        // 1. 查询用户是否存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getPhone, reqDto.getPhone());
        SysUser sysUser = this.getOne(queryWrapper);

        if (sysUser == null) {
            // 这里为了简化，如果没有查询到用户，直接模拟一个自动注册逻辑
            // 在实际业务中可以区分 login 和 register
            sysUser = new SysUser();
            sysUser.setPhone(reqDto.getPhone());
            sysUser.setPassword(reqDto.getPassword()); // 实际应使用 BCrypt 等加密
            sysUser.setRole(0);
            sysUser.setCreditScore(600); // 默认分数
            this.save(sysUser);
        } else {
            // 2. 校验密码 (实际中应为加密比对，如 passwordEncoder.matches)
            if (!sysUser.getPassword().equals(reqDto.getPassword())) {
                throw new RuntimeException("账号或密码错误");
            }
        }

        // 3. 生成 JWT Token
        String token = jwtUtils.generateToken(sysUser.getId(), sysUser.getPhone());

        // 4. 组装返回数据
        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setId(sysUser.getId());
        userInfoVo.setPhone(sysUser.getPhone());
        userInfoVo.setRole(sysUser.getRole());
        userInfoVo.setCreditScore(sysUser.getCreditScore());

        LoginResVo resVo = new LoginResVo();
        resVo.setToken(token);
        resVo.setUserInfo(userInfoVo);

        return resVo;
    }
}
