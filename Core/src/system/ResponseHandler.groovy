package system

import com.sun.istack.internal.Nullable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.apache.logging.log4j.LogManager


class ResponseHandler extends SimpleChannelInboundHandler<Map>{
    
    static logger=LogManager.getLogger(ResponseHandler)
    
    Client client
    Closure initAction
    
    ResponseHandler(Client client,@Nullable Closure initAction){
        this.client=client
        this.initAction=initAction
    }
    
    @Override
    void channelActive(ChannelHandlerContext ctx) throws Exception{
        initAction?.call(client,ctx)
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx,Map msg) throws Exception{
        Map map=msg.clone()
        def result=map.result
        map.removeAll{k,v->['result','file','attachment'].contains(k)}
        logger.info("响应 result=$result message=$map.message data=$map")
        client.response=msg
    }
    
    
    @Override
    void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) throws Exception{
        logger.error cause.localizedMessage
        ctx.close()
    }
}
