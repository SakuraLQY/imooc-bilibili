package com.imooc.bilibili.service;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.dao.UserDao;

import com.imooc.bilibili.domain.*;
import com.imooc.bilibili.domain.constants.UserConstant;
import com.imooc.bilibili.domain.exception.ConditionalException;
import com.imooc.bilibili.service.util.MD5Util;
import com.imooc.bilibili.service.util.RSAUtil;
import com.imooc.bilibili.service.util.TokenUtils;
import com.mysql.jdbc.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;

import static java.lang.Integer.getInteger;


@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAuthService userAuthService;
    /**
     * 新增用户
     * @param user
     */
    public void addUser(User user) {
        String phone = user.getPhone();
        if(StringUtils.isNullOrEmpty(phone)){
            //手机号为Null,抛出异常
            throw new ConditionalException("手机号不能为空！");
        }
        User dbUser = getUserByPhone(phone);
        if(dbUser!=null){
            throw new ConditionalException("用户已注册！");
        }
        //新增操作
        Date now = new Date();
        String salt = String.valueOf(now.getTime());
        String password = user.getPassword();
        String rawPassword;
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionalException("密码解析失败");
        }
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        user.setSalt(salt);
        user.setPassword(md5Password);
        user.setCreateTime(now);
        userDao.addUser(user);
        //添加用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setGender(UserConstant.GENDER_FEMALE);
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setCreateTime(now);
        userDao.addUserInfo(userInfo);
        //进行用户等级权限的添加
        userAuthService.addUserDefaultRole(user.getId());
    }
    public User getUserByPhone(String phone){
       return userDao.getUserByPhone(phone);
    }

    /**
     * 用户登陆
     * @param user:包含phone id password
     * @return
     */
    public String login(User user) throws Exception{
        String phone = user.getPhone()==null ? "":user.getPhone();
        String email = user.getEmail()==null ? "":user.getEmail();
        if(StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)){
           throw new ConditionalException("参数异常QAQ");
        }
        User dbUser = userDao.getUserByPhoneOrEmail(phone,email);
        if(dbUser==null){
            throw new ConditionalException("当前用户不存在");
        }
        String password = user.getPassword();
        String rawPassword;
        //进行密码解密、
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionalException("密码解密失败");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        if(!md5Password.equals(dbUser.getPassword())){
            throw new ConditionalException("密码错误！");
        }
        //获取token
        return TokenUtils.generateToken(dbUser.getId());
    }

//    public User getUserByPhoneOrEmail(String phone, String email) {
//        return userDao.getUserByPhoneOrEmail(phone,email);
//    }

    /**
     * 用户数据登陆
     * @param user
     * @return
     * @throws Exception
     */
    public Map<String, Object> loginForDts(User user) throws Exception{
        String phone = user.getPhone() == null ? "" : user.getPhone();
        String email = user.getEmail() == null ? "" : user.getEmail();
        if(StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)){
            throw new ConditionalException("参数异常！");
        }
        User dbUser = userDao.getUserByPhoneOrEmail(phone, email);
        if(dbUser == null){
            throw new ConditionalException("当前用户不存在！");
        }
        String password = user.getPassword();
        String rawPassword;
        try{
            rawPassword = RSAUtil.decrypt(password);
        }catch (Exception e){
            throw new ConditionalException("密码解密失败！");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        if(!md5Password.equals(dbUser.getPassword())){
            throw new ConditionalException("密码错误！");
        }
        Long userId = dbUser.getId();
        String accessToken = TokenUtils.generateToken(userId);
        String refreshToken = TokenUtils.generateRefreshToken(userId);
        //保存refresh token到数据库
        userDao.deleteRefreshTokenByUserId(userId);
        userDao.addRefreshToken(refreshToken, userId, new Date());
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        return result;

    }


    /**
     * 账户退出登陆
     * @param refreshToken
     * @param userId
     */
    public void logout(String refreshToken, Long userId) {
        userDao.deleteRefreshToken(refreshToken, userId);
    }

    /**
     * 刷新密码令牌
     * @param refreshToken
     * @return
     * @throws Exception
     */
    public String refreshAccessToken(String refreshToken) throws Exception {
        RefreshTokenDetail refreshTokenDetail = userDao.getRefreshTokenDetail(refreshToken);
        if(refreshTokenDetail == null){
            throw new ConditionalException("555","token过期！");
        }
        Long userId = refreshTokenDetail.getUserId();
        return TokenUtils.generateToken(userId);
    }

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    public User getUserByInfo(Long userId) {
         User user = userDao.getUserById(userId);
         UserInfo userInfo =  userDao.getUserInfoByUserId(userId);
         user.setUserInfo(userInfo);
         return user;
    }


    /**
     * 用户修改
     * @param user
     * @return
     */
    public void updateUsers(User user) throws Exception {
        Long id = user.getId();
        User dbUser = userDao.getUserById(id);
        if(dbUser==null){
            throw new ConditionalException("用户不存在");
        }
        if(!StringUtils.isNullOrEmpty(user.getPassword())){
            String rawPassword = RSAUtil.decrypt(user.getPassword());
            String md5Password = MD5Util.sign(rawPassword, dbUser.getSalt(), "UTF-8");
            user.setPassword(md5Password);
        }
        user.setUpdateTime(new Date());
        userDao.updateUsers(user);
    }

    /**
     * 用户表信息的修改
     * @param userInfo
     */
    public void updateUserInfo(UserInfo userInfo){
        userInfo.setUpdateTime(new Date());
        userDao.updateUserInfos(userInfo);
    }

    public User getUserById(Long followingId) {
        return userDao.getUserById(followingId);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList) {

        return userDao.getUserInfoByUserIds(userIdList);
    }

    public PageResult<UserInfo> pageListUserInfos(JSONObject params) {
        Integer no = params.getInteger("no");
        Integer size = params.getInteger("size");
        params.put("start",(no-1)*size);
        params.put("limit",size);
        Integer total =  userDao.pageCountUserInfos(params);
        List<UserInfo>list = new ArrayList<>();
        if(total > 0){
            list = userDao.pageListUserInfos(params);
        }
        return new PageResult<>(total,list) ;
    }


    public List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList) {
        return userDao.batchGetUserInfoByUserIds(userIdList);
    }
}
