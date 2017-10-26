package com.gh.minawebsocket.bean;
public class BinMessageFrame extends WebsocketDataFrame{
	private byte[] content = new byte[0];//内容
	public BinMessageFrame(byte[] content){
		this.content = content;
	}
	public byte[] getContent() {
		return content;
	}
}
