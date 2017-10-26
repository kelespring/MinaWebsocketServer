package com.gh.minawebsocket.handler;

import java.security.MessageDigest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gh.minawebsocket.bean.BinMessageFrame;
import com.gh.minawebsocket.bean.CloseFrame;
import com.gh.minawebsocket.bean.HTTPResponse404Frame;
import com.gh.minawebsocket.bean.HttpRequestFrame;
import com.gh.minawebsocket.bean.HttpWebsocketHandshakeResponseFrame;
import com.gh.minawebsocket.bean.PingFrame;
import com.gh.minawebsocket.bean.PongFrame;
import com.gh.minawebsocket.bean.TextMessageFrame;
import com.gh.minawebsocket.bean.WebSocketFrame;
import com.gh.minawebsocket.core.WebSocketHandler;
import com.gh.minawebsocket.core.WebSocketProtocalConst;

public class ServerHandler extends IoHandlerAdapter {
	WebSocketHandler serverHandler = null;
	private final static Logger log = LogManager
            .getLogger(ServerHandler.class);
	
	public ServerHandler(WebSocketHandler webSocketHandler){
		this.serverHandler = webSocketHandler;
	}
	
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		log.info("客户端{} 建立链接",session.getRemoteAddress());
		session.setAttribute("ip",session.getRemoteAddress().toString());
		serverHandler.onOpen(session);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		log.info("客户端{} 断开",session.getAttribute("ip"));
		serverHandler.onClose(session);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		log.info("客户端{} 请求进入闲置状态....回路即将关闭....",session.getRemoteAddress());
		serverHandler.onIdle(session);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		log.info("客户端{} 异常",session.getAttribute("ip"));
		serverHandler.onError(session);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		log.info("客户端{} 收到消息",session.getRemoteAddress());
		ObjectMapper mapper = new ObjectMapper();
		if(message instanceof HttpRequestFrame){
			HttpRequestFrame httpRequestFrame = (HttpRequestFrame)message;
			log.info("http请求："+mapper.writeValueAsString(httpRequestFrame));
			//执行对应路径的处理器
			//找不到回应404
			if(httpRequestFrame.containsKey("Connection")){
				String conn = (String)httpRequestFrame.get("Connection");
				if(conn.equals("Upgrade")){
					//session.setAttribute("isWebSocket",true);
					String sec_websocket_key = (String)httpRequestFrame.get("Sec-WebSocket-Key");
					String sec_websocket_extensions = (String)httpRequestFrame.get("Sec-WebSocket-Extensions");
					//SHA处理
					sec_websocket_key += WebSocketProtocalConst.SEC_WEBSOCKET_KEY_GUID;
			        MessageDigest cript = MessageDigest.getInstance("SHA-1");
			        cript.reset();
			        cript.update(sec_websocket_key.getBytes("utf8"));
			        byte[] hashedVal = cript.digest();
			        Base64 base64 = new Base64();
			        sec_websocket_key = new String(base64.encodeBase64(hashedVal));
					//返回握手信息
			        HttpWebsocketHandshakeResponseFrame hwrf = new HttpWebsocketHandshakeResponseFrame();
					hwrf.setSecWebsocketAccept(sec_websocket_key);
					hwrf.setSecWebsocketExtensions(sec_websocket_extensions);
					WriteFuture wr = session.write(hwrf);
					log.info("客户端请求升级为websocket协议");
					wr.addListener(new IoFutureListener(){
						@Override
						public void operationComplete(IoFuture future) {
							//发送出去后，设置session管道协议为websocket
							future.getSession().setAttribute("isWebSocket",true);
							log.info("["+session.getId()+"] 已提升为websocket协议");
						}
					});
				}else{
					session.write(new HTTPResponse404Frame());
					session.closeOnFlush();
				}
			}else{
				//不接受其他http请求
				session.write(new HTTPResponse404Frame());
				session.closeOnFlush();
			}
		}else if(message instanceof WebSocketFrame){
			if(message instanceof TextMessageFrame){
				serverHandler.onMessage(session, (TextMessageFrame)message);
			}else if(message instanceof BinMessageFrame){
				serverHandler.onMessage(session, (BinMessageFrame)message);
			}else if(message instanceof CloseFrame){
				//关闭帧，收到断开TCP连接
				session.closeOnFlush();
			}else if(message instanceof PongFrame){
				serverHandler.onPong(session);
			}else if(message instanceof PingFrame){
				serverHandler.onPing(session);
			}
		}
	}

	
}
