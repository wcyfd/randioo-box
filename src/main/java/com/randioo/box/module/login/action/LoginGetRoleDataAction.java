package com.randioo.box.module.login.action;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.protobuf.GeneratedMessage;
import com.randioo.box.module.login.service.LoginService;
import com.randioo.box.protocol.Login.LoginGetRoleDataRequest;
import com.randioo.randioo_server_base.annotation.PTAnnotation;
import com.randioo.randioo_server_base.template.IActionSupport;
import com.randioo.randioo_server_base.utils.SessionUtils;

@Controller
@PTAnnotation(LoginGetRoleDataRequest.class)
public class LoginGetRoleDataAction implements IActionSupport {

	@Autowired
	private LoginService loginService;

	@Override
	public void execute(Object data, IoSession session) {
		LoginGetRoleDataRequest request = (LoginGetRoleDataRequest) data;
		String account = request.getAccount();
		String macAddress = request.getUuid();
		GeneratedMessage sc = loginService.getRoleData(account, macAddress, session);
		SessionUtils.sc(session, sc);
	}

}
