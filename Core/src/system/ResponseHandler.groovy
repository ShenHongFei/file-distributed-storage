package system

import com.sun.istack.internal.Nullable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler


class ResponseHandler extends SimpleChannelInboundHandler<Map>{
    
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
        //todo:log
        def map=msg.collectEntries{
            if(it.key=='file'){
                return [file:it.value.length]
            }else{
                return [it.key,it.value]
            }
        }
        println("$map")
        client.response=msg
    }
    
    
    @Override
    void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) throws Exception{
        cause.printStackTrace()
        println cause.localizedMessage
    }
}
