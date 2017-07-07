package system

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

import java.nio.charset.Charset

class FileClientHandler extends ChannelInboundHandlerAdapter{
    @Override
    void channelActive(ChannelHandlerContext ctx){
        def channel = ctx.channel()
        println("Server "+channel.remoteAddress() +" connected")
        ctx.fireChannelActive()
        /*ctx.writeAndFlush(ctx.alloc().buffer(100).writeCharSequence('echo message',Charset.forName('utf-8'))).addListener(new ChannelFutureListener(){
            @Override
            void operationComplete(ChannelFuture future) throws Exception{
                println 'sent echo message'
            }
        })*/
//        ctx.fireChannelActive()
    }
    
    @Override
    void channelRead(ChannelHandlerContext ctx,Object msg) throws Exception{
        
    }
    
    @Override
    void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) throws Exception{
        println cause.localizedMessage
    }
}