package system

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager

class FileServer{
    
    static logger=LogManager.getLogger(FileServer)
    
    Server               server    = new Server(this,8080,ConnectionType.TCP,null)
    Server               udpServer = new Server(this,8081,ConnectionType.UDP,null)
    File                 fileser   = new File('data/FileServer/files.ser')
    Map<String,NodeInfo> nodes     = [:]
    Map<UUID,FileInfo>   files     = [:]
    
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
    
    
    void nodeReg(ChannelHandlerContext ctx,Map map){
        nodes[map.nodeInfo.name]=map.nodeInfo
    }
    /**
     * 选择主存结点和备份结点
     * 用nodes.value排序,大小比较由NodeInfo实现
     */
    void selectNode(ChannelHandlerContext ctx,Map map){
        NodeInfo main=null
        NodeInfo backup=null
        nodes.findAll{it.value.aliveNow}.toSorted{it.value}.iterator().with{
            if(it.hasNext()) main=next().value
            if(it.hasNext()) backup=next().value
        }
        logger.debug "主结点：$main"
        logger.debug "备份结点：$backup"
        ctx.channel().writeAndFlush([result:true,mainNodeInfo:main,backupNodeInfo:backup])
    }
    
    /**
     * 根据uuid对应FileInfo是否已存在来决定此NodeInfo的类型
     */
    void addFile(ChannelHandlerContext ctx,Map map){
        if(!files[map.uuid]){
            files[map.uuid]=new FileInfo(uuid:map.uuid,main:map.nodeInfo,name:map.fileName,size: map.fileSize)
            logger.info "新增文件 ${files[map.uuid]}"
        }else{
            files[map.uuid].backup=map.nodeInfo
            logger.info "新增备份结点 文件:${map.uuid} 结点：$map.nodeInfo"
        }
        saveFiles()
    }
    
    /**
     * 由于files中的文件对应的结点信息NodeInfo可能是从文件中加载而得，
     * 返回给FileClient时需根据nodes刷新NodeInfo
     */
    def getFileInfo(ChannelHandlerContext ctx,Map map){
        def fileInfo = files[map.uuid]
        if(!fileInfo){
            logger.warn "uuid=$map.uuid 的文件不存在"
            ctx.writeAndFlush([result:false,message:"uuid=$map.uuid 的文件不存在"])
        }
        if(fileInfo.main) fileInfo.main=nodes[fileInfo.main.name]
        if(fileInfo.backup) fileInfo.backup=nodes[fileInfo.backup.name]
        logger.info "请求文件存储结点信息 ${fileInfo}"
        ctx.writeAndFlush([result:true,fileinfo:fileInfo])
        saveFiles()
    }
    
    def remove(ChannelHandlerContext ctx,Map map){
        FileInfo fileInfo=files[map.uuid]
        def main = fileInfo.main
        def backup=fileInfo.backup
        try{
            if(main) new Client(main.address,main.port,ConnectionType.TCP,{Client c,ChannelHandlerContext ct->
                ct.writeAndFlush([action:'remove',uuid:fileInfo.uuid]).addListener({ChannelFuture future->future.channel().close()})
            })
            if(backup){
                new Client(backup.address,backup.port,ConnectionType.TCP,{Client c,ChannelHandlerContext ct->
                    ct.writeAndFlush([action:'remove',uuid:fileInfo.uuid]).addListener({ChannelFuture future->future.channel().close()})
                })
            }
        }catch(any){}
        
        files.remove(map.uuid)
        saveFiles()
    }
    
    def saveFiles(){
        fileser.withObjectOutputStream{
            it.writeObject(files)
        }
    }
}