package system

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket

class FileServer{
    
    Server server =new Server(this,8080,ConnectionType.UDP,null)
    
    static void main(String[] args){
        new FileServer().server.waitClose()
    }
    
    def test(ChannelHandlerContext ctx,Map req){
        println 'success'
        println req
    }
    
    def keepalive(ChannelHandlerContext ctx,DatagramPacket pkt){
        
    }
    
}