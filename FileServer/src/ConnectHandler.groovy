import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class ConnectHandler extends ChannelInboundHandlerAdapter{
    @Override
    void channelActive(ChannelHandlerContext ctx){
        println("Client "+ctx.channel().remoteAddress() + " connected")
        ctx.fireChannelActive()
    }
}