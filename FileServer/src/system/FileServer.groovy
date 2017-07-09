package system

import groovy.time.TimeCategory
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext

class FileServer{
    
    Server                           server    =new Server(this,8080,ConnectionType.TCP,null)
    Server                           udpServer =new Server(this,8081,ConnectionType.UDP,null)
    Map<String,NodeInfo> nodes     =[:]
    
    static void main(String[] args){
        new FileServer().run()
    }
    
    void run(){
        server.waitClose()
    }
    
    def test(ChannelHandlerContext ctx,Map req){
        println 'success'
        println req
    }
    
    def nodeReg(ChannelHandlerContext ctx,Map map){
        nodes[map.nodeinfo.name]=map.nodeinfo
        println nodes
    }
    
    void selectNode(ChannelHandlerContext ctx,Map map){
        NodeInfo best=nodes.findAll{k,v->
            boolean alive=false
            use(TimeCategory){
                alive= v.alive>15.seconds.ago
            }
            alive
        }.min{k,v->v}?.value
        println best
        if(!best){
            ctx.channel().writeAndFlush([result:false,message:'当前无可用存储节点'])
            return
        }
        ctx.channel().writeAndFlush([result:true,nodeinfo:best])
        
        /*ctx.channel().writeAndFlush([result:false,nodeinfo:best]).addListener(new ChannelFutureListener(){
            @Override
            void operationComplete(ChannelFuture future) throws Exception{
                if(future.success){
                    println 'success'
                }else{
                    println 'failed'
                }
            }
        })*/
        
    }
}