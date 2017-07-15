package system

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.util.AttributeKey
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import org.apache.logging.log4j.LogManager
import util.Util

import java.util.concurrent.ConcurrentHashMap

/**
 * 并发接收多个文件，接收一个文件后通知调用者接收完成
 */
class FileReceiver{
    static {
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE)
    }
    static final logger=LogManager.getLogger(FileReceiver)
    Channel serverChannel
    /**
     * 根据InetSocketAddress(唯一标识FileSender)找到一张属性表:
     * 传输前设置：
     *      fileName=xxx
     *      fileSize=xxx
     *      tempFile=xxx
     *      uuid=xxx
     *      finishedCallback 闭包
     *      backupNodeInfo=null/xxx
     * 传输时修改：
     *      receivedLength=0
     *           done
     *           progress
     *      fileChannel=tempFile对应通道
     * 传输完成(包括异常)：
     *      todo:异常处理
     *      调用 finishedCallback
     *      设置 result=true/false
     *      设置 error
     */
    Map<InetSocketAddress,Map<String,Object>> infos=new ConcurrentHashMap<>()
    
    FileReceiver(Integer listenPort){
        serverChannel=new ServerBootstrap().with{
            group(new NioEventLoopGroup(),new NioEventLoopGroup())
            channel(NioServerSocketChannel)
            option(ChannelOption.SO_BACKLOG,128)
            childOption(ChannelOption.SO_KEEPALIVE,false)
            handler(new LoggingHandler(LogLevel.DEBUG))
            childHandler({ (it as SocketChannel).pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>(){
                @Override
                void channelActive(ChannelHandlerContext ctx) throws Exception{
                    InetSocketAddress remoteAddress = ctx.channel().remoteAddress()
                    logger.info "$remoteAddress 已连接"
                    Map<String,Object> info = infos[remoteAddress]
                    ctx.channel().attr(AttributeKey.valueOf('info')).set(info)
                    info.receivedLength=0
                    info.fileChannel=new FileOutputStream(info.tempFile).channel
                }
                @Override
                protected void channelRead0(ChannelHandlerContext ctx,ByteBuf msg) throws Exception{
                    Map info=ctx.channel().attr(AttributeKey.valueOf('info')).get()
                    info.receivedLength+=msg.readableBytes()
                    info.fileChannel.write(msg.nioBuffer())
                    logger.debug "已收到 ${Util.getHumanReadableByteCount(info.receivedLength,false)}"
                    if(info.receivedLength==info.fileSize){
                        logger.info "传输完成 文件名：$info.fileName ,文件大小:${Util.getHumanReadableByteCount(info.fileSize,false)} "
                        info.fileChannel.close()
                        info.finishedCallback(info)
                    }
                }
            })} as ChannelInitializer<SocketChannel>)
            bind(listenPort).sync().channel()
        }
    }
    
    def waitClose(){
        serverChannel.closeFuture().sync()
    }
}