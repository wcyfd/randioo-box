package com.randioo.box.module.login.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.protobuf.GeneratedMessage;
import com.randioo.box.dao.RoleDao;
import com.randioo.box.entity.bo.Role;
import com.randioo.box.entity.po.Client;
import com.randioo.box.module.login.LoginConstant;
import com.randioo.box.module.role.service.RoleService;
import com.randioo.box.protocol.Entity.RoleData;
import com.randioo.box.protocol.Error.ErrorCode;
import com.randioo.box.protocol.Login.LoginGetRoleDataResponse;
import com.randioo.box.protocol.Login.SCLoginOtherSide;
import com.randioo.box.protocol.ServerMessage.SC;
import com.randioo.box.util.Tool;
import com.randioo.randioo_server_base.cache.RoleCache;
import com.randioo.randioo_server_base.db.GameDB;
import com.randioo.randioo_server_base.entity.RoleInterface;
import com.randioo.randioo_server_base.module.login.Facility;
import com.randioo.randioo_server_base.module.login.LoginHandler;
import com.randioo.randioo_server_base.module.login.LoginInfo;
import com.randioo.randioo_server_base.module.login.LoginModelConstant;
import com.randioo.randioo_server_base.module.login.LoginModelService;
import com.randioo.randioo_server_base.service.ObserveBaseService;
import com.randioo.randioo_server_base.template.EntityRunnable;
import com.randioo.randioo_server_base.template.Ref;
import com.randioo.randioo_server_base.utils.SessionUtils;
import com.randioo.randioo_server_base.utils.StringUtils;

@Service("loginService")
public class LoginServiceImpl extends ObserveBaseService implements LoginService {

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private LoginModelService loginModelService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private GameDB gameDB;
    
    @Autowired
    private Client client;

    @Override
    public void init() {
        // 初始化所有已经有过的帐号和昵称
        add(RoleCache.getNameSet(), roleDao.getAllNames());
        add(RoleCache.getAccountSet(), roleDao.getAllAccounts());

        loginModelService.setLoginHandler(new LoginHandlerImpl());
    }

    @Override
    public void initService() {
        loggerinfo("initService");
    }

    private void add(Map<String, String> map, List<String> list) {
        for (String str : list) {
            map.put(str, str);
        }
    }

    private class LoginHandlerImpl implements LoginHandler {

        @Override
        public RoleInterface getRoleInterfaceFromDBById(int roleId) {
            return roleDao.get(roleId);
        }

        @Override
        public RoleInterface getRoleInterfaceFromDBByAccount(String account) {
            return roleDao.getRoleByAccount(account);
        }

        @Override
        public void loginRoleModuleDataInit(RoleInterface roleInterface) {
            // 将数据库中的数据放入缓存中
            Role role = (Role) roleInterface;

            roleService.roleInit(role);
        }

        @Override
        public boolean createRoleCheckAccount(LoginInfo info, Ref<Integer> errorCode) {
            // 账号姓名不可为空
            if (StringUtils.isNullOrEmpty(info.getAccount())) {
                errorCode.set(LoginConstant.CREATE_ROLE_NAME_SENSITIVE);
                return false;
            }

            return true;
        }

        @Override
        public RoleInterface createRole(LoginInfo loginInfo) {
            String account = loginInfo.getAccount();
            // 用户数据
            // 创建用户
            Role role = new Role();
            role.setAccount(account);

            roleService.newRoleInit(role);

            gameDB.getInsertPool().submit(new EntityRunnable<Role>(role) {

                @Override
                public void run(Role entity) {
                    try {
                        roleDao.insert(entity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return role;
        }

        @Override
        public Facility saveFacility(Facility facility) {
            return null;
        }

        @Override
        public void noticeOtherPlaceLogin(Facility oldFacility) {
            IoSession session = oldFacility.getSession();
            SessionUtils.sc(session, SC.newBuilder().setSCLoginOtherSide(SCLoginOtherSide.newBuilder()).build());
        }

        @Override
        public Facility getFacilityFromDB(int roleId, String macAddress) {
            return null;
        }

    }

    @Override
    public GeneratedMessage getRoleData(String account, String macAddress, IoSession ioSession) {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setAccount(account);
        loginInfo.setMacAddress(macAddress);

        Ref<Integer> errorCode = new Ref<>();

        RoleInterface roleInterface = loginModelService.getRoleData(loginInfo, errorCode, ioSession);

        if (roleInterface != null) {
            Role role = (Role) roleInterface;

            return SC.newBuilder()
                    .setLoginGetRoleDataResponse(LoginGetRoleDataResponse.newBuilder().setRoleData(getRoleData(role)))
                    .build();
        }

        ErrorCode errorEnum = null;
        switch (errorCode.get()) {
        case LoginModelConstant.GET_ROLE_DATA_NOT_EXIST:
            errorEnum = ErrorCode.NO_ROLE_DATA;
            break;
        case LoginModelConstant.GET_ROLE_DATA_IN_LOGIN:
            errorEnum = ErrorCode.IN_LOGIN;
            break;
        }
        SC sc = SC.newBuilder()
                .setLoginGetRoleDataResponse(LoginGetRoleDataResponse.newBuilder().setErrorCode(errorEnum.getNumber()))
                .build();

        return sc;
    }

    @Override
    public RoleData getRoleData(Role role) {
        roleService.roleInit(role);

        int roleId = Tool.regExpression(role.getAccount(), "[0-9]*") ? Integer.parseInt(role.getAccount()) : role
                .getRoleId();

        RoleData.Builder builder = RoleData.newBuilder().setRoleId(roleId).setPoint(1000).setSex(1);

        return builder.build();
    }

    @Override
    public Role getRoleById(int roleId) {
        RoleInterface roleInterface = loginModelService.getRoleInterfaceById(roleId);
        return roleInterface == null ? null : (Role) roleInterface;
    }

    @Override
    public Role getRoleByAccount(String account) {
        RoleInterface roleInterface = loginModelService.getRoleInterfaceByAccount(account);
        return roleInterface == null ? null : (Role) roleInterface;
    }
}
