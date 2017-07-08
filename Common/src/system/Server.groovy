package system

import com.sun.istack.internal.Nullable
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.ServerChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

class Server{
    
    Channel serverChannel
    
    Server(server,Integer port,@Nullable Closure initAction,ChannelHandler... extraHandlers){
        serverChannel=new ServerBootstrap().with{
            group(new NioEventLoopGroup(),new NioEventLoopGroup())
            channel(NioServerSocketChannel)
            handler(new LoggingHandler(LogLevel.DEBUG))
            //注册事件处理器
            childHandler({it.pipeline().addLast(
                    new ObjectEncoder(),
                    new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)),
                    new RequestHandler(server,initAction)
                    )} as ChannelInitializer<SocketChannel>)
            option(ChannelOption.SO_BACKLOG,128)
            childOption(ChannelOption.SO_KEEPALIVE,true)
            //start to accept incoming connections.Wait until the server socket is closed.(does not happen)
            bind(port).sync().channel()
        }
    }
    
    def waitClose(){
        serverChannel.closeFuture().sync()
    }
    
}
