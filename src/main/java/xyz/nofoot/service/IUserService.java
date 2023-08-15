package xyz.nofoot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.nofoot.dto.LoginFormDTO;
import xyz.nofoot.dto.Result;
import xyz.nofoot.entity.User;

import javax.servlet.http.HttpSession;

public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);
}
