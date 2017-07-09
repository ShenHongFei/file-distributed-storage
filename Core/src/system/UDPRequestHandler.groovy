package system

import com.sun.istack.internal.Nullable
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
//!不是JDK中的DatagramPacket
import io.netty.channel.socket.DatagramPacket

class UDPRequestHandler extends SimpleChannelInboundHandler<DatagramPacket>{
    
    def server
    Closure initAction
    
    UDPRequestHandler(server,@Nullable Closure initAction){
        this.server=server
        this.initAction=initAction
    }
    
    @Override
    void channelActive(ChannelHandlerContext ctx) throws Exception{
        initAction?.call(ctx)
    }
    
    @Override
    void channelRead0(ChannelHandlerContext ctx,DatagramPacket msg) throws Exception{
        
        def byteBuf = msg.content()
        def outbytes = new ByteArrayOutputStream()
        byteBuf.getBytes(0,outbytes,byteBuf.capacity())
        ByteArrayInputStream bis = new ByteArrayInputStream(outbytes.toByteArray())
        ObjectInput instream = null
        try {
            instream = new ObjectInputStream(bis)
            Object o = instream.readObject()
            try{
                println o
                server.invokeMethod(o.action,[ctx,o].toArray())
            }catch(MissingMethodException e){
                server.defaultMethod(o.action,[ctx,o].toArray())
            }
        } finally {
            try {
                if (instream != null) {
                    instream.close()
                }
            } catch (IOException ex) { }
        }
    
    }
    
    
    @Override
    void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) throws Exception{
        cause.printStackTrace()
        println cause.localizedMessage
    }
}
