package system

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder

class FileClient{
    
    Scanner scanner=new Scanner(System.in)
    
    def run(){
        def channel=new Bootstrap().with{
            group(new NioEventLoopGroup())
            channel(NioSocketChannel)
            handler({it.pipeline().addLast(
                    new ObjectEncoder(),
                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                    new FileClientHandler(),
                    new FileClientCommandHandler())} as ChannelInitializer<SocketChannel>)
            //start to accept incoming connections.Wait until the server socket is closed.(does not happen)
            connect('localhost',8080).sync().channel()
        }
        while(scanner.hasNextLine()){
            channel.writeAndFlush(new Command(method:scanner.nextLine()))
        }
        channel.close().sync()
    }
    
    
    static void main(String... args){
        new FileClient().run()
    }
}