package com.imooc.bilibili.service;


import com.imooc.bilibili.domain.auth.*;
import com.imooc.bilibili.domain.constants.AuthRoleConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAuthService {
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private AuthRoleService authRoleService;

    public UserAuthorities getUserAuthorities(Long userId){
    List<UserRole> userRoleList =  userRoleService.getUserRoleByUserId(userId);
    Set<Long> roleIdSet = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
    //通过role开始获取role表的其他信息
    List<AuthRoleElementOperation>roleElementOperationList =  authRoleService.getRoleElementOperationsByRoleIds(roleIdSet);
    List<AuthRoleMenu>authRoleMenuList = authRoleService.getAuthRoleMenusByRoleIds(roleIdSet);
    UserAuthorities userAuthorities = new UserAuthorities();
    userAuthorities.setRoleElementOperationList(roleElementOperationList);
    userAuthorities.setRoleMenuList(authRoleMenuList);
    return userAuthorities;
    }


    /**
     * 查询authrole的code得到roleId->在进行user-role的添加
     * @param id
     */
    public void addUserDefaultRole(Long id) {
       UserRole userRole = new UserRole();
       //通过等级code拿到对应的roleId字段表
       AuthRole role = authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV0);
       userRole.setUserId(id);
       userRole.setRoleId(role.getId());
       //将roleId suerId createTime传到对应的userRole表里面
       userRoleService.addUserDefaultRole(userRole);
    }
}
