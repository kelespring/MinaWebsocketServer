package com.gh.minawebsocket.codec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.gh.minawebsocket.core.FrameUtils;
import com.gh.minawebsocket.core.Session;
/**
 * 编码器
 * @Author chao
 * @Data 2017-10-24
 */
public class Encoder extends ProtocolEncoderAdapter {
	//用于打印日志信息
    private final static Logger log = LogManager
            .getLogger(Encoder.class);
	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		Session currentSession = new Session(session);
        if(message!=null){
        	out.write(FrameUtils.genFinalFrameByte(message));
		}else{
			throw new Exception("发送的消息不能为空！");
		}
	}

}
