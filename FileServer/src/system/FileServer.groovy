package system

import groovy.time.TimeCategory
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext

class FileServer{
    
    
    
    Server                           server    =new Server(this,8080,ConnectionType.TCP,null)
    Server                           udpServer =new Server(this,8081,ConnectionType.UDP,null)
    File fileser=new File('data/files.ser')
    Map<String,NodeInfo> nodes     =[:]
    Map<UUID,FileInfo>  files=[:] 
    
    static void main(String[] args){
        new FileServer().run()
    }
    
    FileServer(){
        if(fileser.exists()){
            fileser.withObjectInputStream{
                files=it.readObject()
            }
        }
    }
    
    void run(){
        server.waitClose()
    }
    
    
    def nodeReg(ChannelHandlerContext ctx,Map map){
        nodes[map.nodeinfo.name]=map.nodeinfo
        println nodes
    }
    
    void selectNode(ChannelHandlerContext ctx,Map map){
        NodeInfo best=nodes.findAll{k,v->
            use(TimeCategory){
                v.alive>15.seconds.ago
            }
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
    
    def addFile(ChannelHandlerContext ctx,Map map){
        files[map.uuid]=new FileInfo(uuid:map.uuid,main:map.nodeinfo,name:map.name,size:map.size)
        NodeInfo best=nodes.findAll{k,v->
            k!=map.nodeinfo.name&&
            use(TimeCategory){
                v.alive>15.seconds.ago
            }
        }.min{k,v->v}?.value
        println best
        if(!best){
            ctx.channel().writeAndFlush([result:false,message:'当前无可用备份节点'])
            saveFiles()
            return
        }
        ctx.channel().writeAndFlush([result:true,nodeinfo:best])
        saveFiles()
    }
    
    def addBackup(ChannelHandlerContext ctx,Map map){
        
    }
    
    def getFileInfo(ChannelHandlerContext ctx,Map map){
        ctx.writeAndFlush([result:true,fileinfo:files[map.uuid]])
    }
    
    def saveFiles(){
        fileser.withObjectOutputStream{
            it.writeObject(files)
        }
    }
}