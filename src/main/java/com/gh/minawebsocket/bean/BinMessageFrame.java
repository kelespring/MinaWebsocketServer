package com.gh.minawebsocket.bean;
/**
 *  @author chao
 *  @date 2017-10-27
 *  二进制消息
 *
 */
public class BinMessageFrame extends WebsocketDataFrame{
	private byte[] content = new byte[0];//内容
	public BinMessageFrame(byte[] content){
		this.content = content;
	}
	public byte[] getContent() {
		return content;
	}
}
