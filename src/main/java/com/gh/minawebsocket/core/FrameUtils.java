package com.gh.minawebsocket.core;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import com.gh.minawebsocket.bean.BasicFrame;
import com.gh.minawebsocket.bean.BinMessageFrame;
import com.gh.minawebsocket.bean.CloseFrame;
import com.gh.minawebsocket.bean.HTTPResponse404Frame;
import com.gh.minawebsocket.bean.HttpWebsocketHandshakeResponseFrame;
import com.gh.minawebsocket.bean.PingFrame;
import com.gh.minawebsocket.bean.PongFrame;
import com.gh.minawebsocket.bean.TextMessageFrame;
import com.gh.minawebsocket.handler.ServerHandler;
/**
 * 处理帧帮助类
 * @author chao
 * @date 2017-10-27
 */
public class FrameUtils {
	private final static Logger log = LogManager
            .getLogger(FrameUtils.class);
	/**
	 * 生成帧的二进制数(不分片)
	 * @param frame
	 * @return
	 * @throws Exception
	 */
	public static IoBuffer genFinalFrameByte(Object frame) throws Exception{
		IoBuffer ioBuffer = IoBuffer.allocate(1024).setAutoExpand(true);
		if(frame instanceof CloseFrame){
			ioBuffer.put((byte) 0x88);//设置为关闭帧：fin set,Rsv clear,opcode=0x8;
			CloseFrame closeFrame = (CloseFrame)frame;
			ioBuffer.put((byte) 0x02);//掩码FALSE+设置两个长度
			ioBuffer.putShort(closeFrame.getCode()); //消息码1000
		}else if(frame instanceof PingFrame){
			ioBuffer.put((byte) 0x89);//设置为ping帧
			ioBuffer.put((byte) 0x00);//设置掩码+长度位0 
		}else if(frame instanceof PongFrame){
			ioBuffer.put((byte) 0x8A);//设置为pong帧
			ioBuffer.put((byte) 0x00);//设置为掩码+长度位0 
		}else if(frame instanceof BinMessageFrame){
			ioBuffer.put((byte) 0x82);
			BinMessageFrame binMessageFrame = (BinMessageFrame)frame;
			int len = binMessageFrame.getContent().length;
			if(len <= 0){
				throw new Exception("数据不能为空！");
			}else if(len < 126){
				ioBuffer.put((byte)len);
			}else if(len < 65535){
				ioBuffer.put((byte)126);
				ioBuffer.put(intTo2Bytes(len));
			}else if(len < Integer.MAX_VALUE){
				ioBuffer.put((byte)127);
				ioBuffer.put(intTo8Bytes(len));
			}else{
				throw new Exception("数据太大！");
			}
			ioBuffer.put(binMessageFrame.getContent());
		}else if(frame instanceof TextMessageFrame){
			ioBuffer.put((byte) 0x81);//设置为文本帧（不分片）
			TextMessageFrame textMessageFrame = (TextMessageFrame)frame;
			int len = textMessageFrame.getContent().getBytes().length;
			if(len <= 0){
				throw new Exception("数据不能为空！");
			}else if(len < 126){
				ioBuffer.put((byte)len);
			}else if(len < 65536){
				ioBuffer.put((byte)126);
				byte[] lena = intTo2Bytes(len);
				ioBuffer.put(intTo2Bytes(len));
			}else if(len < Integer.MAX_VALUE){
				ioBuffer.put((byte)127);
				ioBuffer.put(intTo8Bytes(len));
			}else{
				throw new Exception("数据太大！");
			}
			ioBuffer.put(textMessageFrame.getContent().getBytes());
		}else if(frame instanceof HTTPResponse404Frame){
			ioBuffer.put(CommonHttpFrameFactory.genHTTP404FrameData());
		}else if(frame instanceof HttpWebsocketHandshakeResponseFrame){
			HttpWebsocketHandshakeResponseFrame httpWebsocketResponseFrame = (HttpWebsocketHandshakeResponseFrame)frame;
			ioBuffer.put(httpWebsocketResponseFrame.getDataBytes());
		}
		ioBuffer.flip();
		return ioBuffer;
	}
	
	
	/**
	 * 只取低16位（因为java没有无符号类型）
	 * @param value
	 * @return
	 */
	public static byte[] intTo2Bytes(int value)   
	{   
	    byte[] src = new byte[2];
	    src[0] =  (byte) ((value>>8) & 0xFF);    
	    src[1] =  (byte) (value & 0xFF);                  
	    return src;   
	}  
	
	public static int byteArray2ToInt(byte[] b) {  
	    return   b[1] & 0xFF | (b[0] & 0xFF) << 8;  
	}  
	
	public static int byteArray2ToShort(byte[] b) {  
	    return   b[1] & 0xFF | (b[0] & 0xFF) << 8;  
	}  
	
	public static int byteArray8ToInt(byte[] b) {  
	    return   b[7] & 0xFF | (b[6] & 0xFF) <<8 | (b[5] & 0xFF) <<16 | (b[4] & 0xFF) << 24;  
	}  
	
	/**
	 * 高32位置0，也就是说最大长度为2的32次方-1(大约512MBytes数据量)
	 * @param value
	 * @return
	 */
	public static byte[] intTo8Bytes(int value)   
	{   
	    byte[] src = new byte[8];  
	    src[0] =  (byte) (0x00);  
	    src[1] =  (byte) (0x00);  
	    src[2] =  (byte) (0x00);  
	    src[3] =  (byte) (0x00);  
	    src[4] =  (byte) ((value>>24) & 0xFF);  
	    src[5] =  (byte) ((value>>16) & 0xFF);  
	    src[6] =  (byte) ((value>>8) & 0xFF);    
	    src[7] =  (byte) (value & 0xFF);                  
	    return src;   
	}
	
	/**
	 * 格林尼治时间
	 * @return
	 */
	static DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
	static{
		df.setTimeZone(TimeZone.getTimeZone("GMT")); // modify Time Zone.
	}
	public static String getGMT() {
		return (df.format(Calendar.getInstance().getTime()));
	}
	
	public static void main(String args[]) throws UnsupportedEncodingException{
		int i = 0x1 | 0x1 <<8;
		System.out.println(i);
	}
}
