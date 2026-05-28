package com.msb.rentcarhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.rentcarhou.common.utils.JwtUtils;
import com.msb.rentcarhou.common.utils.MD5Utils;
import com.msb.rentcarhou.common.utils.UserContext;
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
    private final com.msb.rentcarhou.mapper.UserCertificationMapper userCertificationMapper;

    @Override
    public LoginResVo login(LoginReqDto reqDto) {
        checkLoginParams(reqDto);

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
        UserInfoVo userInfoVo = buildUserInfoVo(sysUser);

        LoginResVo resVo = new LoginResVo();
        resVo.setToken(token);
        resVo.setUserInfo(userInfoVo);

        return resVo;
    }

    @Override
    public void register(RegisterReqDto reqDto) {
        checkRegisterParams(reqDto);

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
    public UserInfoVo getCurrentUserInfo() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("未登录或Token已过期");
        }
        SysUser sysUser = this.getById(userId);
        if (sysUser == null) {
            throw new RuntimeException("用户不存在");
        }
        if (sysUser.getStatus() != null && sysUser.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        return buildUserInfoVo(sysUser);
    }

    @Override
    public Page<SysUser> getUserList(UserQueryDto queryDto) {
        int pageNum = queryDto == null || queryDto.getPage() == null || queryDto.getPage() < 1 ? 1 : queryDto.getPage();
        int pageSize = queryDto == null || queryDto.getPageSize() == null || queryDto.getPageSize() < 1 ? 10 : queryDto.getPageSize();
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        
        if (queryDto != null && StringUtils.hasText(queryDto.getKeyword())) {
            queryWrapper.like(SysUser::getUsername, queryDto.getKeyword())
                        .or()
                        .like(SysUser::getPhone, queryDto.getKeyword());
        }
        queryWrapper.orderByDesc(SysUser::getCreateTime);
        
        Page<SysUser> userPage = this.page(page, queryWrapper);
        userPage.getRecords().forEach(user -> user.setPassword(null));
        return userPage;
    }

    @Override
    public com.msb.rentcarhou.vo.UserCreditInfoVo getCreditInfo() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("未登录或Token已过期");
        }
        
        SysUser sysUser = this.getById(userId);
        if (sysUser == null) {
            throw new RuntimeException("用户不存在");
        }

        com.msb.rentcarhou.vo.UserCreditInfoVo vo = new com.msb.rentcarhou.vo.UserCreditInfoVo();
        int creditScore = sysUser.getCreditScore() != null ? sysUser.getCreditScore() : 600;
        vo.setCreditScore(creditScore);
        vo.setIsDepositWaived(creditScore >= 700);

        // 获取实名认证信息
        com.msb.rentcarhou.entity.UserCertification certification = userCertificationMapper.selectById(userId);
        if (certification != null) {
            vo.setIdCardNo(certification.getIdCardNo());
            vo.setDriverLicenseUrl(certification.getDriverLicenseUrl());
            vo.setAuditStatus(certification.getAuditStatus());
        } else {
            vo.setAuditStatus(0); // 未认证
        }
        
        return vo;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void certify(com.msb.rentcarhou.dto.CertifyReqDto reqDto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("未登录或Token已过期");
        }

        if (reqDto == null || !org.springframework.util.StringUtils.hasText(reqDto.getIdCardNo()) 
            || !org.springframework.util.StringUtils.hasText(reqDto.getDriverLicenseUrl())) {
            throw new RuntimeException("认证参数不完整");
        }

        // 校验身份证格式
        if (!reqDto.getIdCardNo().matches("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$")) {
            throw new RuntimeException("身份证号码格式不正确");
        }

        // 查询是否已认证
        com.msb.rentcarhou.entity.UserCertification certification = userCertificationMapper.selectById(userId);
        if (certification == null) {
            certification = new com.msb.rentcarhou.entity.UserCertification();
            certification.setUserId(userId);
            certification.setIdCardNo(reqDto.getIdCardNo());
            certification.setDriverLicenseUrl(reqDto.getDriverLicenseUrl());
            certification.setAuditStatus(2); // 默认直接通过(2)，符合演示站特征
            userCertificationMapper.insert(certification);
        } else {
            certification.setIdCardNo(reqDto.getIdCardNo());
            certification.setDriverLicenseUrl(reqDto.getDriverLicenseUrl());
            certification.setAuditStatus(2); // 重新认证直接通过
            userCertificationMapper.updateById(certification);
        }

        // 增加信用分到720分，以供测试体验免押特权
        SysUser sysUser = this.getById(userId);
        if (sysUser != null && (sysUser.getCreditScore() == null || sysUser.getCreditScore() < 700)) {
            sysUser.setCreditScore(720); // 实名认证通过后自动提升为信用达标分(720分)，方便体验双免押
            this.updateById(sysUser);
        }
    }

    private void checkLoginParams(LoginReqDto reqDto) {
        if (reqDto == null) {
            throw new RuntimeException("登录参数不能为空");
        }
        if (!StringUtils.hasText(reqDto.getPhone())) {
            throw new RuntimeException("手机号不能为空");
        }
        if (!StringUtils.hasText(reqDto.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }
    }

    private void checkRegisterParams(RegisterReqDto reqDto) {
        if (reqDto == null) {
            throw new RuntimeException("注册参数不能为空");
        }
        if (!StringUtils.hasText(reqDto.getPhone())) {
            throw new RuntimeException("手机号不能为空");
        }
        if (!reqDto.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }
        if (!StringUtils.hasText(reqDto.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }
        if (reqDto.getPassword().length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }
    }

    private UserInfoVo buildUserInfoVo(SysUser sysUser) {
        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setId(sysUser.getId());
        userInfoVo.setPhone(sysUser.getPhone());
        userInfoVo.setUsername(sysUser.getUsername());
        userInfoVo.setStatus(sysUser.getStatus());
        
        // 角色映射: 0-普通租客, 1-门店管理员, 2-系统超管
        String roleStr = "user";
        if (sysUser.getRole() != null) {
            if (sysUser.getRole() == 1) roleStr = "store_admin";
            else if (sysUser.getRole() == 2) roleStr = "admin";
        }
        userInfoVo.setRole(roleStr);
        userInfoVo.setCreditScore(sysUser.getCreditScore());
        return userInfoVo;
    }
}
