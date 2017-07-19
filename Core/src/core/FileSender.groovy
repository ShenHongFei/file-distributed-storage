package core

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import org.apache.logging.log4j.LogManager
import util.Util


/**
 * 基于TCP连接，负责文件实际传输，进度记录
 */
class FileSender{
    
    static final logger=LogManager.getLogger(FileSender)
    static {
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE)
    }
          Channel           channel
          InetSocketAddress receiverSocketAddress
    
    FileSender(InetSocketAddress receiverSocketAddress,Integer localPort){
        this.receiverSocketAddress=receiverSocketAddress
        channel=new Bootstrap().with{
            group(new NioEventLoopGroup())
            handler(new LoggingHandler(LogLevel.DEBUG))
            channel(NioSocketChannel)
            option(ChannelOption.SO_KEEPALIVE,false)
            handler({Channel ch -> ch.pipeline().addLast(new ChunkedWriteHandler())} as ChannelInitializer<SocketChannel>)
            connect(receiverSocketAddress,new InetSocketAddress(localPort)).sync().channel()
        }
    }
    
    ChannelFuture send(File file){
        channel.writeAndFlush(new DefaultFileRegion(file,0,file.size()), channel.newProgressivePromise()).addListener(new ChannelProgressiveFutureListener() {
            void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                def totalString=Util.getHumanReadableByteCount(total,false)
                def progressString=Util.getHumanReadableByteCount(progress,false)
                if (total < 0) {
                    println("发送进度: $progressString")
                }else {
                    println("发送进度: ${Math.floor(progress/total*100)}%  $progressString/$totalString")
                }
            }
    
            void operationComplete(ChannelProgressiveFuture future) {
                println  "发送完成"
                future.channel().close()
            }
        })
    }
    
}
