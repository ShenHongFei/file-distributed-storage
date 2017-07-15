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
        Map map=msg.clone()
        def action=map.action
        map.removeAll{k,v->['action','file','attachment'].contains(k)}
        map.remove('action');map.remove('file');map.remove('attachment')
        logger.info("请求 action=$action params=$map")
        if(action=='nodeReg'){
            logger.debug("结点续命 action=$action params=$map")
        }else{
            
        }
        
        try{
            server.invokeMethod(action,[ctx,msg].toArray())
        }catch(MissingMethodException e){
            server.defaultMethod(action,[ctx,msg].toArray())
        }
    }
}
