package system

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