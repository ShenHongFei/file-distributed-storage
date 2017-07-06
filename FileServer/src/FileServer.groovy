import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

class FileServer{
    
    void run(){
        EventLoopGroup bossGroup = new NioEventLoopGroup()
        EventLoopGroup workerGroup = new NioEventLoopGroup()
        
        new ServerBootstrap().with{
            group(bossGroup,workerGroup)
            channel(NioServerSocketChannel)
            childHandler(new ChannelInitializer<SocketChannel>(){
                @Override
                void initChannel(SocketChannel ch) throws Exception{
                    ch.pipeline().addLast(new ConnectHandler())
                }
            })
            option(ChannelOption.SO_BACKLOG,128)
            childOption(ChannelOption.SO_KEEPALIVE,true)
            bind(8080)
        }.with{channelFuture ->
            
            //start to accept incoming connections.
            sync()
            
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            channel().closeFuture().sync()
        }
        workerGroup.shutdownGracefully()
        bossGroup.shutdownGracefully()
    }
    
    static void main(String[] args) throws Exception{
        new FileServer().run()
    }
}