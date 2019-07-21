package com.java.study.nio.nettyTimeServer;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TimeClientHandler extends ChannelInboundHandlerAdapter {

	private final ByteBuf firstMessage;
	
	public TimeClientHandler(){
		byte[] req = "QUERY TIME ORDER".getBytes();
		firstMessage = Unpooled.buffer(req.length);
		firstMessage.writeBytes(req);
	}
	
	public void ChannelActive(ChannelHandlerContext ctx) throws Exception  {
		System.out.println("连上服务器了");
		ctx.writeAndFlush(firstMessage);
	}
	
	public void ChannelRead(ChannelHandlerContext ctx,Object msg) throws Exception{
		ByteBuf buf = (ByteBuf) msg;
		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		String body = new String(req,"UTF-8");
		System.out.println("Now is :"+body);
	}
	
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}
	
	public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
		cause.printStackTrace();
		ctx.close();
	}
	
}
