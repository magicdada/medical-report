package com.medical.controller;

import com.medical.common.ResultCode;
import com.medical.common.ResultMessage;
import com.medical.common.ServiceException;
import com.medical.common.security.AuthUser;
import com.medical.common.security.UserContext;
import com.medical.common.util.ResultUtil;
import com.medical.common.security.Token;
import com.medical.common.security.TokenUtil;
import com.medical.entity.dos.Doctor;
import com.medical.entity.dto.DoctorUpdateDTO;
import com.medical.service.DoctorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 认证接口
 * @author wangda
 * @since 2026/08/08
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 医生登录
     *
     * @param username 用户名
     * @param password 密码
     * @return token
     */
    @PostMapping("/login")
    public ResultMessage<Token> login(@NotNull(message = "用户名不能为空") @RequestParam String username,
                                      @NotNull(message = "密码不能为空") @RequestParam String password) {
        return ResultUtil.data(doctorService.login(username, password));
    }

    /**
     * 医生注册
     *
     * @param username   用户名
     * @param password   密码
     * @param realName   真实姓名
     * @param department 科室
     * @return 医生信息
     */
    @PostMapping("/register")
    public ResultMessage<Doctor> register(@NotNull(message = "用户名不能为空") @RequestParam String username,
                                          @NotNull(message = "密码不能为空") @RequestParam String password,
                                          @RequestParam(required = false) String realName,
                                          @RequestParam(required = false) String department) {
        return ResultUtil.data(doctorService.register(username, password, realName, department));
    }

    /**
     * 刷新token
     *
     * @param refreshToken 刷新token
     * @return 新的token
     */
    @GetMapping("/refresh/{refreshToken}")
    public ResultMessage<Object> refreshToken(@NotNull(message = "刷新token不能为空") @PathVariable String refreshToken) {
        return ResultUtil.data(tokenUtil.refreshToken(refreshToken));
    }

    /**
     * 获取当前登录医生信息
     *
     * @return 医生信息
     */
    @GetMapping("/info")
    public ResultMessage<Doctor> info() {
        AuthUser authUser = UserContext.getCurrentUser();
        if (authUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        Doctor doctor = doctorService.getById(authUser.getId());
        return ResultUtil.data(doctor);
    }

    /**
     * 更新个人信息
     *
     * @param dto 更新信息
     * @return 医生信息
     */
    @PutMapping("/update")
    public ResultMessage<Doctor> updateInfo(@RequestBody @Valid DoctorUpdateDTO dto) {
        AuthUser authUser = UserContext.getCurrentUser();
        if (authUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        return ResultUtil.data(doctorService.updateInfo(authUser.getId(), dto));
    }

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 结果
     */
    @PutMapping("/password")
    public ResultMessage<Object> updatePassword(@NotNull(message = "旧密码不能为空") @RequestParam String oldPassword,
                                                @NotNull(message = "新密码不能为空") @RequestParam String newPassword) {
        AuthUser authUser = UserContext.getCurrentUser();
        if (authUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        doctorService.updatePassword(authUser.getId(), oldPassword, newPassword);
        return ResultUtil.success();
    }

    /**
     * 退出登录
     *
     * @return 结果
     */
    @PostMapping("/logout")
    public ResultMessage<Object> logout() {
        AuthUser authUser = UserContext.getCurrentUser();
        if (authUser != null) {
            doctorService.logout(authUser.getId());
        }
        return ResultUtil.success();
    }
}