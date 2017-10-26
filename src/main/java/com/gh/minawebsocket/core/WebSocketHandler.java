package com.gh.minawebsocket.core;

import org.apache.mina.core.session.IoSession;

import com.gh.minawebsocket.bean.BinMessageFrame;
import com.gh.minawebsocket.bean.TextMessageFrame;

/**
 * websocketHandler interface
 * @author chao
 *
 */
public interface WebSocketHandler {
	public void onOpen(IoSession session);
	public void onClose(IoSession session);
	public void onMessage(IoSession session,TextMessageFrame textMessageFrame);
	public void onMessage(IoSession session,BinMessageFrame binMessageFrame);
	public void onError(IoSession session);
	public void onIdle(IoSession session);
	public void onPing(IoSession session);
	public void onPong(IoSession session);
}
