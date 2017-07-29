package com.randioo.box.util.vote;

import java.util.Map;
import java.util.Set;

import com.randioo.box.util.vote.VoteBox.VoteResult;
import com.randioo.randioo_server_base.template.Function;

public interface VoteStrategy {
	/**
	 * 过滤投票
	 * 
	 * @param voter
	 * @param applyer
	 * @return
	 * @author wcy 2017年7月17日
	 */
	public boolean filterVoter(String voter, String applyer);

	/**
	 * 投票
	 * 
	 * @param voter
	 * @param agree
	 * @param map
	 * @param joiners
	 * @param generatedFunction
	 * @param applyer
	 * @return
	 * @author wcy 2017年7月17日
	 */
	public VoteResult vote(String voter, boolean agree, Map<String, Boolean> map, Set<String> joiners,
			Function generatedFunction, String applyer);
}
