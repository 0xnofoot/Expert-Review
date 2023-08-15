package xyz.nofoot.service.impl;

import xyz.nofoot.entity.UserInfo;
import xyz.nofoot.mapper.UserInfoMapper;
import xyz.nofoot.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
