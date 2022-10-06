package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.UserCoinDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserCoinService {
    @Autowired
    private UserCoinDao userCoinDao;

    public Integer getUserCoinAmount(Long userId) {
        return userCoinDao.getUserCoinAmount(userId);
    }


    public void updateUserCoinsAmount(Long userId, Integer amount) {
        Date updateTime = new Date();
        userCoinDao.updateUserCoinAmount(userId,amount,updateTime);
    }
}
