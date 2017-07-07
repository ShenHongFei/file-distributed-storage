package system

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class FileClientCommandHandler extends SimpleChannelInboundHandler<Command>{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx,Command msg) throws Exception{
        
    }
    
    @Override
    void channelActive(ChannelHandlerContext ctx) throws Exception{
        println 'channelActive'
        ctx.writeAndFlush(new Command(method:'echo object',args:[aaa:'bbb'])).addListener({
            if(it.success){
                println 'sent success'
            }else{
                println 'failed'
            }} as ChannelFutureListener)
        println 'sent?'
    }
}
