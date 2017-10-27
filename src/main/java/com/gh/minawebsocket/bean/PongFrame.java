package com.gh.minawebsocket.bean;
/**
 * pong响应帧
 * @author chao
 * @data 2017-10-27
 */
public class PongFrame extends WebsocketControlFrame{
	private String address = "";
	public PongFrame(String address) {
		this.address = address;
	}
	@Override
	public String toString() {
		return "client:"+address+" Pong...";
	}
	
}
