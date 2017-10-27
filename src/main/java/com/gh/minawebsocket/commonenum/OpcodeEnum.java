package com.gh.minawebsocket.commonenum;
/**
 * opcode操作枚举类
 * @author chao
 * @date 2017-10-27
 *
 */
public enum OpcodeEnum {
	//关闭类
	Close_Normal_Closure((short)1000),//正常关闭
	Close_Going_Away((short)1001),//离开关闭
	Close_Protocol_Error((short)1002),//协议错误关闭
	Close_Data_Error((short)1003);//数据错误关闭
	private short code;
	private OpcodeEnum(short code){
		this.code = code;
	}
	public short getValue(){
		return this.code;
	}
	//其他
}
