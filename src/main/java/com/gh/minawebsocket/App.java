package com.gh.minawebsocket;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.gh.minawebsocket.bean.BinMessageFrame;
import com.gh.minawebsocket.bean.TextMessageFrame;
import com.gh.minawebsocket.codec.CodecFactory;
import com.gh.minawebsocket.core.WebSocketHandler;
import com.gh.minawebsocket.handler.ServerHandler;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	//第一步 创建一个NioSocketAcceptor 对象  
        NioSocketAcceptor acceptor=new NioSocketAcceptor();  
        //第二步设置handler  
        acceptor.setHandler(new ServerHandler(new WebSocketHandler() {
			
			@Override
			public void onPong(IoSession session) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPing(IoSession session) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onOpen(IoSession session) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onMessage(IoSession session, BinMessageFrame binMessageFrame) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onMessage(IoSession session, TextMessageFrame textMessageFrame) {
				// TODO Auto-generated method stub
				System.out.println(textMessageFrame.getContent());
			}
			
			@Override
			public void onIdle(IoSession session) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(IoSession session) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onClose(IoSession session) {
				// TODO Auto-generated method stub
				
			}
		}));  
        //第三步,获取拦截器，发来的消息都需要通过拦截器拦截之后才能接收到  
        //添加一个拦截器对数据进行加解码
        acceptor.getFilterChain().addLast("executorFilter",new ExecutorFilter());; 
        acceptor.getFilterChain().addLast("codeFilter",new ProtocolCodecFilter(new CodecFactory()));; 
        acceptor.getFilterChain().addLast("logFilter",new LoggingFilter());;  
        //第四步，绑定端口号  
        try {
			acceptor.bind(new InetSocketAddress(9898));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    }
}
