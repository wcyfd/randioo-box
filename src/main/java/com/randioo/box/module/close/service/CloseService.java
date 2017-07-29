package com.randioo.box.module.close.service;

import com.randioo.box.entity.bo.Role;
import com.randioo.randioo_server_base.service.BaseServiceInterface;

public interface CloseService extends BaseServiceInterface {
	public void asynManipulate(Role role);

	void roleDataCache2DB(Role role, boolean mustSave);
}
