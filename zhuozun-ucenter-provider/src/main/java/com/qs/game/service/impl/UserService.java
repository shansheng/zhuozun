package com.qs.game.service.impl;

import com.qs.game.mapper.UserMapper;
import com.qs.game.model.User;
import com.qs.game.service.IUserService;
import org.springframework.stereotype.Service;
import com.qs.game.base.basemapper.IBaseMapper;
import com.qs.game.base.baseservice.AbstractBaseService;

import javax.annotation.Resource;

/**
 * 用户业务模块
 */
@Service
public class UserService extends AbstractBaseService<User,Long> implements IUserService {


    @Resource
    private UserMapper userMapper;

    @Resource
    public void setMapper(IBaseMapper<User, Long> mapper) {
        super.mapper = mapper;
    }

}
