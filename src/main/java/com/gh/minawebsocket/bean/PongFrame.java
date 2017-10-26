package com.gh.minawebsocket.bean;

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
