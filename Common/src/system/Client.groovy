package system

import com.sun.istack.internal.Nullable
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

class Client{
    
    final ReentrantLock requestLock = new ReentrantLock()
    final Condition     requestGet  = requestLock.newCondition()
          Map      response
          Channel       channel
    
    Client(String serverAddress,Integer port,@Nullable Closure initAction,ChannelHandler... extraHandlers){
        channel=new Bootstrap().with{
            group(new NioEventLoopGroup())
            channel(NioSocketChannel)
            handler({Channel ch->ch.pipeline().addLast(
                    new ObjectEncoder(),
                    new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)),
                    new ResponseHandler(this,initAction),
                    *extraHandlers)
                    } as ChannelInitializer<SocketChannel>)
            //start to accept incoming connections.Wait until the server socket is closed.(does not happen)
            connect(serverAddress,port).sync().channel()
        }
    }
    
    void setRequest(Map req){
        channel.writeAndFlush(req)
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
