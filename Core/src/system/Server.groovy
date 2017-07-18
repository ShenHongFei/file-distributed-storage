package system

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
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory

/**
 * 抽象 Server，根据请求调用 业务Server 的方法
 */
class Server{
    
    static{
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE)
    }
    
    Channel serverChannel
    
    /**
     * @param server 业务服务器对象
     * @param initAction 连接建立后执行，闭包参数为 BusinessServer ..,ChannelHandlerContext ..
     */
    Server(server,Integer port,ConnectionType type=ConnectionType.TCP,Closure initAction=null,ChannelHandler... extraHandlers=null){
        if(type==ConnectionType.TCP){
                            //ServerBootstrap 初始化options,attrs
            serverChannel=new ServerBootstrap().with{
                /**
                 * 设置 Server的 ServerChannel 和 客户端连接对应Channel 的事件循环
                 * Set the {@link io.netty.channel.EventLoopGroup} for the parent (acceptor) and the child (client). These
                 * EventLoopGroups are used to handle all the events and IO for {@link io.netty.channel.ServerChannel} and
                 * {@link Channel}'s.
                 * 一个事件Event属于一个Task,可以被线程池比如{@link java.util.concurrent.Executors#newScheduledThreadPool(16)}调度执行
                 * 实际上当acceptor accept 连接后，从Channel对应的EventLoopGroup中选择空闲的EventLoop为该连接所有的I/O操作服务
                 * 
                 * NioEventLoopGroup继承关系--NioEventLoopGroup多线程事件循环管理组
                 * {@link io.netty.channel.nio.NioEventLoopGroup}
                 * {@link io.netty.channel.MultithreadEventLoopGroup} 
                 * {@link io.netty.util.concurrent.AbstractEventExecutorGroup} 
                 * {@link io.netty.util.concurrent.EventExecutorGroup}
                 * {@link java.util.concurrent.ScheduledExecutorService}
                 * 
                 * NioEventLoop继承关系--NioEventLoop一个线程的事件循环
                 * {@link io.netty.channel.nio.NioEventLoop} {@link io.netty.channel.EventLoop} 负责处理一个Channel所有的I/O事件
                 * {@link io.netty.channel.SingleThreadEventLoop} 由一个线程处理
                 * {@link io.netty.util.concurrent.SingleThreadEventExecutor}
                 * {@link io.netty.util.concurrent.AbstractScheduledEventExecutor} {@link io.netty.util.concurrent.EventExecutor}
                 * {@link java.util.concurrent.AbstractExecutorService}
                 * {@link java.util.concurrent.ScheduledExecutorService}
                 *
                 *                                                 I/O Request
                 *                                            via {@link Channel} or
                 *                                        {@link io.netty.channel.ChannelHandlerContext}
                 *                                                      |
                 *  +---------------------------------------------------+---------------+
                 *  |                           ChannelPipeline         |               |
                 *  |                                                  \|/              |
                 *  |    +---------------------+            +-----------+----------+    |
                 *  |    | Inbound Handler  N  |            | Outbound Handler  1  |    |
                 *  |    +----------+----------+            +-----------+----------+    |
                 *  |              /|\                                  |               |
                 *  |               |                                  \|/              |
                 *  |    +----------+----------+            +-----------+----------+    |
                 *  |    | Inbound Handler N-1 |            | Outbound Handler  2  |    |
                 *  |    +----------+----------+            +-----------+----------+    |
                 *  |              /|\                                  .               |
                 *  |               .                                   .               |
                 *  | ChannelHandlerContext.fireIN_EVT() ChannelHandlerContext.OUT_EVT()|
                 *  |        [ method call]                       [method call]         |
                 *  |               .                                   .               |
                 *  |               .                                  \|/              |
                 *  |    +----------+----------+            +-----------+----------+    |
                 *  |    | Inbound Handler  2  |            | Outbound Handler M-1 |    |
                 *  |    +----------+----------+            +-----------+----------+    |
                 *  |              /|\                                  |               |
                 *  |               |                                  \|/              |
                 *  |    +----------+----------+            +-----------+----------+    |
                 *  |    | Inbound Handler  1  |            | Outbound Handler  M  |    |
                 *  |    +----------+----------+            +-----------+----------+    |
                 *  |              /|\                                  |               |
                 *  +---------------+-----------------------------------+---------------+
                 *                  |                                  \|/
                 *  +---------------+-----------------------------------+---------------+
                 *  |               |                                   |               |
                 *  |       [ Socket.read() ]                    [ Socket.write() ]     |
                 *  |                                                                   |
                 *  |  Netty Internal I/O Threads (Transport Implementation)            |
                 *  +-------------------------------------------------------------------+
                 * 
                 * 综上：
                 * NioEventLoopGroup本质上是一个 {@link java.util.concurrent.ScheduledExecutorService } ，
                 * 实际的实现通常是由Executors中的工具方法/默认线程池来实现 {@link java.util.concurrent.Executors#newScheduledThreadPool(16)}
                 * 底层的Channel I/O 事件交由它来处理并分配给某一 线程/EventLoop 执行，通过加入Handler的方式实现业务逻辑
                 */
                group(new NioEventLoopGroup(),new NioEventLoopGroup())
                //配置工厂类ChannelFactory，以便在某事件发生时 new NioServerSocketChannel
                channel(NioServerSocketChannel)
                //最大并发处理的连接请求数（积压）,多余的连接 connection refused
                option(ChannelOption.SO_BACKLOG,128)
                //ServerChannel accept后子channel的配置选项
                childOption(ChannelOption.SO_KEEPALIVE,true)
                handler(new LoggingHandler(LogLevel.DEBUG))
    
                /**
                 * 当对应客户端连接的channel被注册到EventLoop后，比如在这里是NioEventLoopGroup中的EventLoop，注册事件处理器
                 * Channel中有一个pipeline属性，作为Handler的容器
                 */
                childHandler({(it as SocketChannel).pipeline().addLast(
                        new ObjectEncoder(),
                        new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)),
                        new RequestHandler(server,initAction),
                        *extraHandlers
                )} as ChannelInitializer<SocketChannel>)
    
                /**
                 * 绑定端口，获得ChannelFuture，调用sync()使得它完成(此时的Future状态为done)，最后调用channel()获得channel
                 */
                bind(port).sync().channel()
            }
        }else{
            serverChannel=new Bootstrap().with{
                group(new NioEventLoopGroup())
                channel(NioDatagramChannel)
                handler(new LoggingHandler(LogLevel.DEBUG))
                handler(new UDPRequestHandler(server,initAction))
                bind(port).sync().channel()
            }
        }
    }
    
    
    def waitClose(){
        serverChannel.closeFuture().sync()
    }
    
}
