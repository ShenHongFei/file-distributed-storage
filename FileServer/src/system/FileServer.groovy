package system

import io.netty.channel.ChannelHandlerContext

class FileServer{
    
    Server server =new Server(this,8080,null)
    
    static void main(String[] args){
        new FileServer().server.waitClose()
    }
    
    def test(ChannelHandlerContext ctx,Map req){
        println 'success'
        println req.attachment.length
        ctx.writeAndFlush(result:true,attachment:new File('D:\\essential-netty-in-action\\images\\Figure 14.2 Real-world Memcached request and response headers.jpg').bytes)
    }
    
}