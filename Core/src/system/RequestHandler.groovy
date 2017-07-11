package system

import com.sun.istack.internal.Nullable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.apache.logging.log4j.LogManager


class RequestHandler extends SimpleChannelInboundHandler<Map>{
    
    static logger=LogManager.getLogger(RequestHandler)
    def server
    Closure initAction
    
    RequestHandler(server,@Nullable Closure initAction){
        this.server=server
        this.initAction=initAction
    }
    
    @Override
    void channelActive(ChannelHandlerContext ctx) throws Exception{
        initAction?.call(server,ctx)
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx,Map msg) throws Exception{
        def map=msg.collectEntries{
            if(it.key=='file'){
                return [file:it.value.length]
            }else{
                return [it.key,it.value]
            }
        }
        if(msg.action=='nodeReg'){
            logger.debug("${msg.action}($map)")
        }else{
            logger.info("${msg.action}($map)")
        }
        
        try{
            server.invokeMethod(msg.action,[ctx,msg].toArray())
        }catch(MissingMethodException e){
            server.defaultMethod(msg.action,[ctx,msg].toArray())
        }
        
    }
    
    @Override
    void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) throws Exception{
        logger.error cause.localizedMessage
        throw cause
    }
}
