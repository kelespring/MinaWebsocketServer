package com.gh.minawebsocket.bean;

import com.gh.minawebsocket.commonenum.OpcodeEnum;

public class CloseFrame extends WebsocketControlFrame{
	short code;
	private String address = "";
	public CloseFrame(){
		this.code = OpcodeEnum.Close_Normal_Closure.getValue();
	}
	public CloseFrame(short code){
		this.code = code;
	}
	public CloseFrame(String address,short code){
		this.address = address;
		this.code = code;
	}
	public short getCode() {
		return code;
	}
	
	@Override
	public String toString() {
		return "CloseFrame [code=" + code + ", address=" + address + "]";
	}
	
}
