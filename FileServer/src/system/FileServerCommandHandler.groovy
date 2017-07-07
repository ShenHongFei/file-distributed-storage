package system

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class FileServerCommandHandler extends SimpleChannelInboundHandler<Command>{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx,Command msg) throws Exception{
        println 'channelRead0'
        println msg
    }
}
