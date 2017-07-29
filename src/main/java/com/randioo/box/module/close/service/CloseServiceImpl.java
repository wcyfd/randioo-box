package com.randioo.box.module.close.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.randioo.box.entity.bo.Role;
import com.randioo.box.module.login.service.LoginService;
import com.randioo.randioo_server_base.db.GameDB;
import com.randioo.randioo_server_base.service.BaseService;
import com.randioo.randioo_server_base.template.EntityRunnable;
import com.randioo.randioo_server_base.utils.TimeUtils;

@Service("closeService")
public class CloseServiceImpl extends BaseService implements CloseService {

    @Autowired
    private LoginService loginService;

    @Autowired
    private GameDB gameDB;

    @Override
    public void asynManipulate(Role role) {
        if (role == null)
            return;

        loggerinfo(role, "[account:" + role.getAccount() + ",name:" + role.getName() + "] manipulate");

        role.setOfflineTimeStr(TimeUtils.getDetailTimeStr());

        if (!gameDB.isUpdatePoolClose()) {
            gameDB.getUpdatePool().submit(new EntityRunnable<Role>(role) {
                @Override
                public void run(Role role) {
                    roleDataCache2DB(role, true);
                }
            });
        }
    }

    @Override
    public void roleDataCache2DB(Role role, boolean mustSave) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
            loggererror(role, "id:" + role.getRoleId() + ",account:" + role.getAccount() + ",name:" + role.getName()
                    + "] save error", e);
        }
    }

}
