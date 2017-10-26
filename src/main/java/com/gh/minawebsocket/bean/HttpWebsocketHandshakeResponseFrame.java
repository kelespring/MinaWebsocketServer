package com.gh.minawebsocket.bean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;

import com.gh.minawebsocket.core.FrameUtils;
import com.gh.minawebsocket.core.HttpProtocalConst;

public class HttpWebsocketHandshakeResponseFrame extends HttpResponseFrame{
//	private String responseCode = "";
//	private String upgrade = "";
//	private String connection = "";
	private String secWebsocketAccept = "";
	private String secWebsocketExtensions = "";
//	private String data = "";
	
	
	/**
	 * 	Hypertext Transfer Protocol
	 *  HTTP/1.1 101 \r\n
	 *  Upgrade: websocket\r\n
	 *  Connection: upgrade\r\n
	 *  Sec-WebSocket-Accept: 1CBbdVUHx2vwNyVlvn3NtOfP1rs=\r\n
	 *  Sec-WebSocket-Extensions: permessage-deflate;client_max_window_bits=15\r\n
	 *  Date: Tue, 24 Oct 2017 15:37:40 GMT\r\n
	 *  \r\n
	 * 生成HTTP协议帧
	 * @return
	 */
	public IoBuffer getDataBytes(){
		IoBuffer byteBuffer = IoBuffer.allocate(1024).setAutoExpand(true);
		StringBuilder httpStr = new StringBuilder();
		httpStr.append(HttpProtocalConst.HTTP_PROTOCAL_VERSION).append(" ").append(HttpProtocalConst.HTTP_SWITCHING_PROTOCALS_CODE).append(" \r\n")
			   .append("Upgrade: websocket\r\n")
			   .append("Connection: upgrade\r\n")
		       .append("Sec-WebSocket-Accept: ").append(secWebsocketAccept).append("\r\n");
		if(StringUtils.isNotEmpty(secWebsocketExtensions)){
			httpStr.append("Sec-WebSocket-Extensions: ").append(secWebsocketExtensions).append("=15").append("\r\n");
		}
		httpStr.append("Date: ").append(FrameUtils.getGMT()).append("\r\n\r\n");
		byteBuffer.put(httpStr.toString().getBytes());
		byteBuffer.flip();
		return byteBuffer;
	}
	
	

	public String getSecWebsocketAccept() {
		return secWebsocketAccept;
	}

	public void setSecWebsocketAccept(String secWebsocketAccept) {
		this.secWebsocketAccept = secWebsocketAccept;
	}

	public String getSecWebsocketExtensions() {
		return secWebsocketExtensions;
	}

	public void setSecWebsocketExtensions(String secWebsocketExtensions) {
		this.secWebsocketExtensions = secWebsocketExtensions;
	}
	
	public static void main(String args[]){
	}
	
	
}
