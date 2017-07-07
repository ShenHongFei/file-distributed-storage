package system

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

class FileServer{
    
    static void main(String[] args){
        new FileServer().run()
    }
    
    void run(){
        new ServerBootstrap().with{
            group(new NioEventLoopGroup(),new NioEventLoopGroup())
            channel(NioServerSocketChannel)
            handler(new LoggingHandler(LogLevel.DEBUG))
            //注册事件处理器
            childHandler({it.pipeline().addLast(
                    new ObjectEncoder(),
                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                    new FileServerHandler(),
                    new FileServerCommandHandler()
                    )} as ChannelInitializer<SocketChannel>)
            option(ChannelOption.SO_BACKLOG,128)
            childOption(ChannelOption.SO_KEEPALIVE,true)
            //start to accept incoming connections.Wait until the server socket is closed.(does not happen)
            bind(8080).sync().channel().closeFuture().sync()
        }
    }
}