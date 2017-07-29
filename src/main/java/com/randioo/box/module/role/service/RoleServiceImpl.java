package com.randioo.box.module.role.service;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.protobuf.GeneratedMessage;
import com.randioo.box.dao.RoleDao;
import com.randioo.box.entity.bo.Role;
import com.randioo.box.module.login.LoginConstant;
import com.randioo.box.module.login.service.LoginService;
import com.randioo.box.protocol.Entity.RoleData;
import com.randioo.box.protocol.Error.ErrorCode;
import com.randioo.box.protocol.Role.RoleGetRoleDataResponse;
import com.randioo.box.protocol.Role.RoleRenameResponse;
import com.randioo.box.protocol.ServerMessage.SC;
import com.randioo.randioo_platform_sdk.RandiooPlatformSdk;
import com.randioo.randioo_platform_sdk.entity.AccountInfo;
import com.randioo.randioo_platform_sdk.exception.AccountErrorException;
import com.randioo.randioo_server_base.cache.RoleCache;
import com.randioo.randioo_server_base.config.GlobleConfig;
import com.randioo.randioo_server_base.config.GlobleConfig.GlobleEnum;
import com.randioo.randioo_server_base.db.IdClassCreator;
import com.randioo.randioo_server_base.module.role.RoleHandler;
import com.randioo.randioo_server_base.module.role.RoleModelService;
import com.randioo.randioo_server_base.sensitive.SensitiveWordDictionary;
import com.randioo.randioo_server_base.service.ObserveBaseService;
import com.randioo.randioo_server_base.template.Ref;
import com.randioo.randioo_server_base.utils.StringUtils;

@Service("roleService")
public class RoleServiceImpl extends ObserveBaseService implements RoleService {

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private IdClassCreator idClassCreator;

	@Autowired
	private RoleModelService roleModelService;

	@Autowired
	private LoginService loginService;

	@Autowired
	private RandiooPlatformSdk randiooPlatformSdk;

	@Override
	public void init() {
		Integer maxRoleId = roleDao.getMaxRoleId();
		idClassCreator.initId(Role.class, maxRoleId == null ? 0 : maxRoleId);
		roleModelService.setRoleHandler(new RoleHandler() {

			Pattern p = Pattern.compile("^[a-zA-Z\u4e00-\u9fa5]+$");

			@Override
			public boolean checkNewNameIllege(String name, Ref<Integer> errorCode) {

				if (name.length() >= 10) {
					errorCode.set(LoginConstant.CREATE_ROLE_NAME_TOO_LONG);
					return false;
				}

				if (SensitiveWordDictionary.containsSensitiveWord(name)) {
					errorCode.set(LoginConstant.CREATE_ROLE_NAME_SENSITIVE);
					return false;
				}

				if (RoleCache.getNameSet().containsKey(name)) {
					errorCode.set(LoginConstant.CREATE_ROLE_NAME_REPEATED);
					return false;
				}

				// 检查特殊字符
				if (!p.matcher(name).find()) {
					errorCode.set(LoginConstant.CREATE_ROLE_NAME_CHAR);
					return false;
				}

				return true;

			}
		});
	}

	@Override
	public void initService() {
		if (GlobleConfig.Boolean(GlobleEnum.DEBUG)) {
			randiooPlatformSdk.setDebug(true);
		}
	}

	@Override
	public void newRoleInit(Role role) {
		// 设置战场的第一章
		role.setRoleId(idClassCreator.getId(Role.class));
		role.setName(role.getAccount());

		initRoleDataFromHttp(role);
	}

	@Override
	public void roleInit(Role role) {
		initRoleDataFromHttp(role);
	}

	@Override
	public GeneratedMessage rename(Role role, String name) {
		Ref<Integer> errorCode = new Ref<>();
		boolean success = roleModelService.rename(role, name, errorCode);
		if (!success) {
			ErrorCode errorCodeEnum = null;
			switch (errorCode.get()) {
			case LoginConstant.CREATE_ROLE_NAME_SENSITIVE:
				errorCodeEnum = ErrorCode.NAME_SENSITIVE;
				break;
			case LoginConstant.CREATE_ROLE_NAME_REPEATED:
				errorCodeEnum = ErrorCode.NAME_REPEATED;
				break;
			case LoginConstant.CREATE_ROLE_NAME_TOO_LONG:
				errorCodeEnum = ErrorCode.NAME_TOO_LONG;
				break;
			case LoginConstant.CREATE_ROLE_NAME_CHAR:
				errorCodeEnum = ErrorCode.NAME_SPECIAL_CHAR;
			}
			return SC.newBuilder()
					.setRoleRenameResponse(RoleRenameResponse.newBuilder().setErrorCode(errorCodeEnum.getNumber()))
					.build();
		}

		return SC.newBuilder().setRoleRenameResponse(RoleRenameResponse.newBuilder()).build();
	}

	@Override
	public void setHeadimgUrl(Role role, String headImgUrl) {
	}

	@Override
	public void setRandiooMoney(Role role, int randiooMoney) {
	}

	@Override
	public void initRoleDataFromHttp(Role role) {

		String name = null;
		String headImgUrl = null;
		int money = -1;
		int sex = 0;

//		try {
//			AccountInfo accountInfo = randiooPlatformSdk.getAccountInfo(role.getAccount());
//			name = accountInfo.nickName;
//			headImgUrl = accountInfo.headImgUrl;
//			money = accountInfo.randiooMoney;
//			sex = accountInfo.sex;
//		} catch (AccountErrorException e) {
//			loggererror("account " + role.getAccount() + " not on platform");
//		} catch (Exception e) {
//			loggererror("randiooPlatformSdk get account error ", e);
//		}
//		if (money == -1)
//			money = 0;

		role.setName(StringUtils.isNullOrEmpty(name) ? "guest" + role.getRoleId() : name);
	}

	@Override
	public boolean addRandiooMoney(Role role, int money) {
		try {
			randiooPlatformSdk.addMoney(role.getAccount(), money);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public GeneratedMessage getRoleData(String account) {
		Role role = loginService.getRoleByAccount(account);
		RoleData roleData = loginService.getRoleData(role);

		return SC.newBuilder().setRoleGetRoleDataResponse(RoleGetRoleDataResponse.newBuilder().setRoleData(roleData))
				.build();
	}
}
