package system

import com.sun.istack.internal.Nullable
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.internal.SocketUtils

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
//IMPORTANT
import io.netty.channel.socket.DatagramPacket

import static system.ConnectionType.*
class Client{
    
    final   ReentrantLock  requestLock = new ReentrantLock()
    final   Condition      requestGet  = requestLock.newCondition()
            Map            response
            Channel        channel
            ConnectionType type
    InetSocketAddress      serverSocketAddress
    
    
    Client(String serverAddress,Integer port,ConnectionType type,@Nullable Closure initAction,ChannelHandler... extraHandlers){
        serverSocketAddress=SocketUtils.socketAddress(serverAddress,port)
        this.type=type
        channel=new Bootstrap().with{
            group(new NioEventLoopGroup())
            handler(new LoggingHandler(LogLevel.INFO))
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
            
            //start to accept incoming connections.Wait until the server socket is closed.(does not happen)
            connect(serverSocketAddress).sync().channel()
        }
    }
    
    void setRequest(Map req){
        if(type==TCP){
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
    
    
    Map getResponse(){
        //实现等待请求返回
        requestLock.lock()
        try{
            requestGet.await()
        }finally{
            requestLock.unlock()
        }
        response
    }
    
    void setResponse(Map resp){
        requestLock.lock()
        this.@response=resp
        requestGet.signal()
        requestLock.unlock()
    }
    
    def waitClose(){
        channel.closeFuture().sync()
    }
    
}
