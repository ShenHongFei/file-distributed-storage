package system

import com.sun.istack.internal.Nullable
import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.stream.ChunkedWriteHandler

class Server{
    
    Channel serverChannel
    
    /**
     * @param server 业务服务器对象
     * @param initAction 连接建立后执行，闭包参数为 server,ChannelHandlerContext
     */
    Server(server,Integer port,ConnectionType type,@Nullable Closure initAction,ChannelHandler... extraHandlers){
        if(type==ConnectionType.TCP){
            serverChannel=new ServerBootstrap().with{
                group(new NioEventLoopGroup(),new NioEventLoopGroup())
                channel(NioServerSocketChannel)
                childOption(ChannelOption.SO_KEEPALIVE,true)
                option(ChannelOption.SO_BACKLOG,128)
                handler(new LoggingHandler(LogLevel.INFO))
                //注册事件处理器
                childHandler({it.pipeline().addLast(
                        new ObjectEncoder(),
                        new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)),
                        new RequestHandler(server,initAction),
                        *extraHandlers
                )} as ChannelInitializer<SocketChannel>)
        
        
                //start to accept incoming connections.Wait until the server socket is closed.(does not happen)
                bind(port).sync().channel()
            }
        }else{
            serverChannel=new Bootstrap().with{
                group(new NioEventLoopGroup())
                channel(NioDatagramChannel)
                handler(new LoggingHandler(LogLevel.INFO))
                handler(new UDPRequestHandler(server,initAction))
                bind(port).sync().channel()
            }
        }
    }
    
    def setResponse(){
        
    }
    
    def waitClose(){
        serverChannel.closeFuture().sync()
    }
    
}
