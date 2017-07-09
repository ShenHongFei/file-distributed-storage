package system

import com.sun.istack.internal.Nullable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class RequestHandler extends SimpleChannelInboundHandler<Map>{
    
    def server
    Closure initAction
    
    RequestHandler(server,@Nullable Closure initAction){
        this.server=server
        this.initAction=initAction
    }
    
    @Override
    void channelActive(ChannelHandlerContext ctx) throws Exception{
        initAction?.call(ctx,server)
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx,Map msg) throws Exception{
        //todo:log
        def map=msg.collectEntries{
            if(it.key=='attachment'){
                return [attachment:it.value.length]
            }else{
                return [it.key,it.value]
            }
        }
        println("${msg.action}($map)")
        try{
            server.invokeMethod(msg.action,[ctx,msg].toArray())
        }catch(MissingMethodException e){
            server.defaultMethod(msg.action,[ctx,msg].toArray())
        }
        
    }
    
    @Override
    void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) throws Exception{
        cause.printStackTrace()
        println cause.localizedMessage
    }
}
