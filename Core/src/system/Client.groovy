package system

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.util.internal.SocketUtils
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import org.apache.logging.log4j.LogManager

import java.util.concurrent.*

import static system.ConnectionType.TCP


//IMPORTANT
class Client{
    
    static final logger=LogManager.getLogger(Client)
    
    static{
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE)
    }
    
    Boolean requestGet=false
    Map response
    Channel channel
    ConnectionType type
    InetSocketAddress serverSocketAddress
    
    /**
     * @param initAction 连接建立后执行，闭包参数为 Client ..,ChannelHandlerContext ..
     */
    Client(String serverAddress,Integer serverPort,ConnectionType type=TCP,Closure initAction=null,ChannelHandler... extraHandlers=null){
        serverSocketAddress=SocketUtils.socketAddress(serverAddress,serverPort)
        this.type=type
        channel=new Bootstrap().with{
            group(new NioEventLoopGroup())
            handler(new LoggingHandler(LogLevel.DEBUG))
            if(type==TCP){
                channel(NioSocketChannel)
                option(ChannelOption.SO_KEEPALIVE,true)
                handler({Channel ch->ch.pipeline().addLast(
                        new ObjectEncoder(),
                        new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)),
                        new ResponseHandler(this,initAction),
                        *extraHandlers)
                        } as ChannelInitializer<SocketChannel>)
            }else{
                channel(NioDatagramChannel)
            }
            /**
             * I/O 操作都是异步的，返回一个通道未来ChannelFuture，调用sync()等待未来，最后channel()获得通道
             */
            connect(serverSocketAddress).sync().channel()
        }
    }
    
    void setRequest(Map req){
        requestGet=false
        if(type==TCP){
            //断线重连
            if(!channel.active){
                logger.info "尝试断线重连..."
                channel=new Bootstrap().with{
                    group(new NioEventLoopGroup())
                    handler(new LoggingHandler(LogLevel.DEBUG))
                    channel(NioSocketChannel)
                    option(ChannelOption.SO_KEEPALIVE,true)
                    handler({Channel ch->ch.pipeline().addLast(
                            new ObjectEncoder(),
                            new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)),
                            new ResponseHandler(this,null))
                    } as ChannelInitializer<SocketChannel>)
                    connect(serverSocketAddress).sync().channel()
                }
            }
            channel.writeAndFlush(req)
        }else{
            ByteArrayOutputStream bos = new ByteArrayOutputStream()
            ObjectOutput out = null
            try {
                out = new ObjectOutputStream(bos)
                out.writeObject(req)
                out.flush()
                byte[] bytes = bos.toByteArray()
                channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes),serverSocketAddress))
            } finally {
                try {
                    bos.close()
                } catch (IOException ex) { }
            }
        }
    }
    
    /**
     * [nioEventLoopGroup-2-1] [poolName-poolId-threadId]
     * 实现条件等待：EventLoop线程收到服务器的Response并为当前对象赋值前阻塞，直到超时,默认超时时间=5秒
     */
    Map getResponse(){
        getResponse(5)
    }
    
    synchronized Map waitResponse(){
        while(!requestGet){
            logger.debug '线程阻塞等待服务器响应'
            wait()
        }
        this.@response
    }
    
    /**
     * 等待服务器响应，自定义超时时间
     */
    Map getResponse(Integer timeOutSeconds){
        ExecutorService executor = Executors.newSingleThreadExecutor()
        Future<Map> future = executor.submit(this.&waitResponse as Callable<Map>)
        try {
            future.get(timeOutSeconds,TimeUnit.SECONDS)
        } catch (TimeoutException e) {
            future.cancel(true)
            logger.error "等待响应 $timeOutSeconds s 后超时，强行结束"
            throw e
        }finally{
            executor.shutdown()
        }
    }
    
    /**
     * 由nioEventLoopGroup线程池中的线程执行
     */
    synchronized void setResponse(Map resp){
        requestGet=true
        this.@response=resp
        notifyAll()
    }
    
    def shutdown(){
        channel.close().sync()
    }
}
