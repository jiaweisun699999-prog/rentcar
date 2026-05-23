package com.msb.rentcarhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.rentcarhou.common.utils.JwtUtils;
import com.msb.rentcarhou.common.utils.MD5Utils;
import com.msb.rentcarhou.dto.LoginReqDto;
import com.msb.rentcarhou.dto.RegisterReqDto;
import com.msb.rentcarhou.dto.UserQueryDto;
import com.msb.rentcarhou.entity.SysUser;
import com.msb.rentcarhou.mapper.SysUserMapper;
import com.msb.rentcarhou.service.SysUserService;
import com.msb.rentcarhou.vo.LoginResVo;
import com.msb.rentcarhou.vo.UserInfoVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
            throw new RuntimeException("账号不存在");
        } 
        
        // 2. 校验密码 (对用户输入的明文密码进行MD5加密后进行比对)
        String encryptPassword = MD5Utils.encrypt(reqDto.getPassword());
        if (!sysUser.getPassword().equals(encryptPassword)) {
            throw new RuntimeException("账号或密码错误");
        }

        // 3. 校验状态
        if (sysUser.getStatus() != null && sysUser.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 4. 生成 JWT Token
        String token = jwtUtils.generateToken(sysUser.getId(), sysUser.getPhone());

        // 5. 组装返回数据
        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setId(sysUser.getId());
        userInfoVo.setPhone(sysUser.getPhone());
        userInfoVo.setUsername(sysUser.getUsername());
        
        // 角色映射: 0-普通租客, 1-门店管理员, 2-系统超管
        String roleStr = "user";
        if (sysUser.getRole() != null) {
            if (sysUser.getRole() == 1) roleStr = "store_admin";
            else if (sysUser.getRole() == 2) roleStr = "admin";
        }
        userInfoVo.setRole(roleStr);
        userInfoVo.setCreditScore(sysUser.getCreditScore());

        LoginResVo resVo = new LoginResVo();
        resVo.setToken(token);
        resVo.setUserInfo(userInfoVo);

        return resVo;
    }

    @Override
    public void register(RegisterReqDto reqDto) {
        // 1. 校验手机号是否已存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getPhone, reqDto.getPhone());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("手机号已被注册");
        }

        // 2. 插入新用户
        SysUser sysUser = new SysUser();
        sysUser.setPhone(reqDto.getPhone());
        // 对明文密码进行 MD5 加密后存入数据库
        sysUser.setPassword(MD5Utils.encrypt(reqDto.getPassword()));
        sysUser.setUsername("用户" + reqDto.getPhone().substring(7)); // 默认昵称
        sysUser.setRole(0);
        sysUser.setStatus(1); // 默认正常
        sysUser.setCreditScore(600); // 默认分数
        
        this.save(sysUser);
    }

    @Override
    public Page<SysUser> getUserList(UserQueryDto queryDto) {
        Page<SysUser> page = new Page<>(queryDto.getPage(), queryDto.getPageSize());
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDto.getKeyword())) {
            queryWrapper.like(SysUser::getUsername, queryDto.getKeyword())
                        .or()
                        .like(SysUser::getPhone, queryDto.getKeyword());
        }
        queryWrapper.orderByDesc(SysUser::getCreateTime);
        
        return this.page(page, queryWrapper);
    }
}
