package system

import groovy.time.TimeCategory
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager

class FileServer{
    
    static logger=LogManager.logger
    
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
    }
    
    void selectNode(ChannelHandlerContext ctx,Map map){
        NodeInfo best=nodes.findAll{k,v->
            use(TimeCategory){
                v.alive>30.seconds.ago
            }
        }.min{a,b->a.value<=>b.value}?.value
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
                v.alive>30.seconds.ago
            }
        }.min{a,b->a.value<=>b.value}?.value
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
        files[map.uuid].backup=map.nodeinfo
        saveFiles()
    }
    
    def getFileInfo(ChannelHandlerContext ctx,Map map){
        ctx.writeAndFlush([result:true,fileinfo:files[map.uuid]])
    }
    
    def remove(ChannelHandlerContext ctx,Map map){
        FileInfo fileInfo=files[map.uuid]
        def main = fileInfo.main
        def backup=fileInfo?.backup
        def mainclient=new Client(main.address,main.port,ConnectionType.TCP,{Client c,ChannelHandlerContext ct->
            ct.writeAndFlush([action:'remove',uuid:fileInfo.uuid])
        })
        if(backup){
            def backupclient=new Client(backup.address,backup.port,ConnectionType.TCP,{Client c,ChannelHandlerContext ct->
                //todo:为什么不能用c.request=xxx
                ct.writeAndFlush([action:'remove',uuid:fileInfo.uuid])
            })
        }
        files.remove(map.uuid)
        saveFiles()
    }
    
    def saveFiles(){
        fileser.withObjectOutputStream{
            it.writeObject(files)
        }
    }
}