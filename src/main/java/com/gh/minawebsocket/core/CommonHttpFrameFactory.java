package com.gh.minawebsocket.core;
import org.apache.mina.core.buffer.IoBuffer;
public class CommonHttpFrameFactory {
	
	/**
	Hypertext Transfer Protocol
    HTTP/1.1 404 \r\n
    Content-Type: text/html;charset=utf-8\r\n
    Content-Language: en\r\n
    Content-Length: 1099\r\n
        [Content length: 1099]
    Date: Wed, 25 Oct 2017 07:41:21 GMT\r\n
    \r\n
    File Data: 1099 bytes
	*/
	static int http_response_404_length = 0;
	static StringBuilder http_response_404 = null;
	static{
		//404返回数据
		http_response_404 = new StringBuilder();
		http_response_404.append("<html><head><title>只允许websocket接入</title></head><body><h1>只允许websocket接入</h1></body></html>");
	    http_response_404_length = http_response_404.toString().getBytes().length;
	}
	
	/**
	 * 生成404HTTP帧
	 * @return
	 */
	public static IoBuffer genHTTP404FrameData(){
		IoBuffer byteBuffer = IoBuffer.allocate(1024).setAutoExpand(true);
		StringBuilder httpStr = new StringBuilder();
		httpStr.append(HttpProtocalConst.HTTP_PROTOCAL_VERSION).append(" ").append(HttpProtocalConst.HTTP_RESPONSE_404).append(" \r\n")
		       .append("Content-Type: ").append(HttpProtocalConst.HTTP_RESPONSE_CONTENTTYPE_HTMLORTEXT).append("\r\n")
		       .append("Content-Language: ").append("en\r\n")
		       .append("Content-Length: ").append(http_response_404_length).append("\r\n");
		httpStr.append("Date: ").append(FrameUtils.getGMT()).append("\r\n\r\n");
		byteBuffer.put(httpStr.toString().getBytes());
		byteBuffer.put(http_response_404.toString().getBytes());
		byteBuffer.flip();
		return byteBuffer;
	}
	
}
