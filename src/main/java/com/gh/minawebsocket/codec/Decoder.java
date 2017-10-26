package com.gh.minawebsocket.codec;

import java.awt.image.VolatileImage;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.security.cert.CertPathValidatorException.Reason;
import java.util.HashMap;
import java.util.Map;

import javax.print.attribute.standard.MediaSize.Other;
import javax.sound.sampled.LineListener;
import javax.swing.plaf.metal.OceanTheme;

import org.apache.commons.lang3.math.Fraction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler;

import com.gh.minawebsocket.bean.BinMessageFrame;
import com.gh.minawebsocket.bean.CloseFrame;
import com.gh.minawebsocket.bean.HttpRequestFrame;
import com.gh.minawebsocket.bean.PongFrame;
import com.gh.minawebsocket.bean.TextMessageFrame;
import com.gh.minawebsocket.commonenum.OpcodeEnum;
import com.gh.minawebsocket.core.FrameUtils;
import com.gh.minawebsocket.core.Session;
import com.gh.minawebsocket.core.WebSocketProtocalConst;
/**
 * @author chao
 * @date 2017-10-24
 * 解码器
 */
public class Decoder extends CumulativeProtocolDecoder {
	//用于打印日志信息
    private final static Logger log = LogManager
            .getLogger(Decoder.class);
    private final int MAX_DATA_SIZE = 10*8*1024*1024;//(10MByte)
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        Session currentSession = new Session(session);
		if(!currentSession.isWebSocket()){
			/**
			 * 这部分是HTTP请求处理
			 */
			log.info("http请求进入");
			 //http请求，检查是否握手请求
			IoBuffer tempBuffer = IoBuffer.allocate(1024).setAutoExpand(true);
			byte[] cr = new byte[4];
			byte currByte = 0;
			in.mark();
			int i = 0;
			while(in.hasRemaining()){
				i++;
				currByte = in.get();
				tempBuffer.put(currByte);
				cr[3]=cr[2];
				cr[2]=cr[1];
				cr[1]=cr[0];
				cr[0]=currByte;
				if(i>MAX_DATA_SIZE){
					session.write("数据量太大！");
					session.closeOnFlush();
					return false;
				}
				if(cr[0]=='\n' && cr[1]=='\r' && cr[2]=='\n' && cr[3]=='\r'){
					tempBuffer.flip();
					Map httpParams = getHttpFrame(tempBuffer);
					if(httpParams.containsKey("Content-Length")){
						int dataLength =0;
						dataLength = Integer.parseInt((String) httpParams.get("Content-Length"));
						if(in.remaining()<dataLength){
							in.reset();
							return false;
						}else{
							byte[] packArray = new byte[dataLength]; 
							in.get(packArray);
							httpParams.put("data", packArray);
						}
					}
					out.write(httpParams);
					if(in.remaining()>0){
						//组装完，还有数据，返回true，告诉mina继续调用decode
						return true;
					}else{
						//组装完，没有数据了，返回false,告诉mina歇着吧，等下次数据到来再调用
						return false;
					}
				}
			}
			//如果还是不能组装数据就重置，并返回false，等着下次数据到来再说
			in.reset();
			return false;
        }else if(currentSession.isWebSocket()){
        	/**
        	 * 这部分是websocket请求
        	 */
        	//websocket请求
        	log.info("websocket请求进入 address:{}",session.getRemoteAddress());
        	in.mark();
        	if(in.remaining()>1){
        		byte b1 = in.get();//第一位确定FIN（是否最后一帧数）
        		byte b2 = in.get();
        		//判断是否最后一个帧
        		if((b1&0x80) == 0x80){
        			//掩码不为1拒绝
            		if((b2&0x80) == 0x80){
            			//确定长度------start
            			int len = b2&0x7f;
            			if(len<126){
            				//
            			}else if(len==126){
            				if(in.remaining()>=6){
            					byte[] lenBytes = new byte[2];
            					in.get(lenBytes);
            					len = FrameUtils.byteArray2ToInt(lenBytes);
            				}else{
            					in.reset();
            					return false;
            				}
            			}else if(len==127){
            				if(in.remaining()>=10){
            					byte[] lenBytes = new byte[4];
            					in.get(lenBytes);
            					len = FrameUtils.byteArray8ToInt(lenBytes);
            				}else{
            					in.reset();
            					return false;
            				}
            			}else{
            				in.reset();
            				//非法的
            				closeClient(session);
							return false;
            			}
            			//结束长度------start
            			//掩码位
            			byte mask[] = new byte[4];
                        for (int i = 0; i < 4; i++) {
                            mask[i] = in.get();
                        }
                        
						switch (b1 & 0x0f) {
							case 0x01:
								//文字类
								if(in.remaining()>=len){
									byte[] data = new byte[len];
									in.get(data);
									TextMessageFrame textMessageFrame = new TextMessageFrame(
											new String(
													unMaskData(mask,data)//解码
													)
											);
									out.write(textMessageFrame);
								}else{
									in.reset();
									return false;
								}
								break;
							case 0x02:
								// 字节类
								if(in.remaining()>=len){
									byte[] data = new byte[len];
									in.get(data);
									BinMessageFrame binMessageFrame = new BinMessageFrame(
												unMaskData(mask,data)//解码
											);
									out.write(binMessageFrame);
								}else{
									in.reset();
									return false;
								}
								break;
							case 0x08:
								// 断开链接
								short reasonCode = 1000;
								if(len>0){
									//如果有数据就读取后扔掉（这个一般不可能，但是安全期间做个处理）
									if(in.remaining()>=len){
										byte[] data = new byte[2];//code是16位的数字
										reasonCode = in.get();//把数据扔掉
									}else{
										in.reset();
										return false;
									}
								}
								CloseFrame closeFrame = new CloseFrame(session.getRemoteAddress().toString(),reasonCode);
								out.write(closeFrame);
								break;
							case 0x09:
								// ping
								break;
							case 0x0a:
								// pong
								if(len>0){
									//如果pong有数据就读取后扔掉（这个一般不可能，但是安全期间做个处理）
									if(in.remaining()>=len){
										byte[] data = new byte[len];
										in.get(data);//把数据扔掉
									}else{
										in.reset();
										return false;
									}
								}
								PongFrame pongFrame = new PongFrame(session.getAttribute("ip").toString());
								out.write(pongFrame);
								
								break;
	
							default:
								// 不支持
								// 协议错误
								closeClient(session);
								break;
						}
						
						if(in.remaining()>0){
							return true;//还有数据，继续
						}else{
							return false;//没有数据了，等待下一次数据
						}
            		}else{
            			// 不支持
            			TextMessageFrame textMessageFrame = new TextMessageFrame("数据未掩码处理！");
            			session.write(textMessageFrame);
						// 协议错误
						closeClient(session);
						return false;
            		}
        			
        		}else if((b1&0x80) == 0x00){
        			//不支持分片
        			TextMessageFrame textMessageFrame = new TextMessageFrame("暂时不支持分片！");
        			session.write(textMessageFrame);
        			closeClient(session);
        			return false;
        		}
        	}
        }
		return false;
	}
	
	//去掩码
	public byte[] unMaskData(byte mask[],byte[] unMaskData){
        for (int i = 0; i < unMaskData.length; i++) {
            byte maskedByte = unMaskData[i];
            unMaskData[i] = (byte) (maskedByte ^ mask[i % 4]);
        }
		return unMaskData;
	}
	
	public HttpRequestFrame getHttpFrame(IoBuffer ioBuffer) throws CharacterCodingException{
		HttpRequestFrame httpParams = new HttpRequestFrame();
		String httpRequest = ioBuffer.getString(Charset.forName("UTF-8").newDecoder());
		String[] items = httpRequest.split("\r\n");
		for(int i=0;i<items.length;i++){
			if(i==0){
				httpParams.put("request-method",items[i]);
			}else{
				String[] params = items[i].split(": ");
				httpParams.put(params[0], params[1]);
			}
		}
		return httpParams;
	}
	
	public void closeClient(IoSession session){
		CloseFrame closeFrame = new CloseFrame(OpcodeEnum.Close_Normal_Closure.getValue());
		WriteFuture writeFuture = session.write(closeFrame);
		writeFuture.addListener(new IoFutureListener() {
			@Override
			public void operationComplete(IoFuture future) {
				future.getSession().closeOnFlush();
			}
		});
	}
	
}
