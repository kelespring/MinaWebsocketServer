package com.gh.minawebsocket.bean;
/**
 * 文本帧
 * @author chao
 * @data 2017-10-27
 */
public class TextMessageFrame extends WebsocketDataFrame{
	private String content="";//内容

	public TextMessageFrame(String content){
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "TextMessageFrame [content=" + content + "]";
	}
	
	public static void main(String args[]){
		int i = 0x7F&0x7f;
		System.out.println(i);
	}
	
}
