package com.gh.minawebsocket.core;

import org.apache.mina.core.session.IoSession;

/**
 * 自定义session扩展
 * @author chao
 * @date 2017-10-27
 */
public class Session {
	IoSession session;
	public Session(IoSession session){
		this.session = session;
	}
	/**
	 * 是否是websocket协议
	 * @return
	 */
	public boolean isWebSocket(){
		if(session.containsAttribute("isWebSocket")){
			return true;
		}else{
			return false;
		}
	}
}
