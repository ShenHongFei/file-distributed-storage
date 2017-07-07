package system

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder

class FileClient{
    
    def echoMessage(){
        
    }
    
    def run(){
        new Bootstrap().with{
            group(new NioEventLoopGroup())
            channel(NioSocketChannel)
            handler({it.pipeline().addLast(
                    new ObjectEncoder(),
                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                    new FileClientHandler(),
                    new FileClientCommandHandler())} as ChannelInitializer<SocketChannel>)
            //start to accept incoming connections.Wait until the server socket is closed.(does not happen)
            connect('localhost',8080).sync().channel().closeFuture().sync()
        }
    }
    
    
    static void main(String... args){
        new FileClient().run()
    }
}
