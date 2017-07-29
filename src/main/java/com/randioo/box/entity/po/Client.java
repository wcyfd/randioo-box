package com.randioo.box.entity.po;

import com.randioo.randioo_server_base.entity.RoleInterface;
import com.randioo.randioo_server_base.utils.SessionUtils;

/**
 * 
 * @author wcy 2017年7月28日
 *
 */
public class Client{

    
    public void receiveCommand(Command cmd){
        
    }
    public void replaceScene(RoleInterface roleInterface) {
        SessionUtils.sc(roleInterface.getRoleId(), null);
    }
}
