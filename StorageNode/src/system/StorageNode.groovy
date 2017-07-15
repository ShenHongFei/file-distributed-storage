package system

import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import util.Util

import java.nio.file.Files

class StorageNode{
    
    static final logger=LogManager.getLogger(StorageNode)
    
    Properties   config =new Properties()
    File         dataDir
    File         tempDir
    File         fileDir
    Client       udpClient
    Client       client
    FileReceiver receiver=new FileReceiver(0)
    Server       server
    NodeInfo     nodeInfo
    
    static void main(String... args){
        new StorageNode(args[0]).run()
    }
    
    StorageNode(String configLocation){
        config.load(new FileInputStream(configLocation))
        (dataDir=new File(config.rootDir)).mkdirs()
        (tempDir=new File(dataDir,'temp')).mkdirs()
        (fileDir=new File(dataDir,'file')).mkdirs()
        nodeInfo=new NodeInfo(address:config.nodeIP,name:config.nodeName,port:config.nodePort as Integer)
        udpClient=new Client(config.fileServerIP,config.fileServerUdpPort as Integer,ConnectionType.UDP,null)
        client=new Client(config.fileServerIP,config.fileServerPort as Integer,ConnectionType.TCP,null)
        server=new Server(this,config.nodePort as Integer,ConnectionType.TCP,null)
    }
    
    def run(){
        while(true){
            nodeInfo.alive=new Date()
            udpClient.request=[action:'nodeReg',nodeInfo:nodeInfo]
            sleep(15*1000)
        }
    }
    
    /**
     * 兼具 存储、备份的功能
     * synchronized 保证空间分配正常
     */
    synchronized void upload(ChannelHandlerContext ctx,Map map){
        def uuid=map.uuid?:UUID.randomUUID()
        //todo:检查剩余空间
        receiver.infos[new InetSocketAddress((ctx.channel().remoteAddress() as InetSocketAddress).address,map.senderPort)]=[fileName:map.fileName,fileSize:map.fileSize,uuid:uuid,tempFile:new File(tempDir,uuid.toString()),finishedCallback:this.&uploadFinished,backupNodeInfo:map.backupNodeInfo]
        //发送 允许上传 响应
        ctx.channel().writeAndFlush([result:true,uuid:uuid,receiverPort:(receiver.serverChannel.localAddress() as InetSocketAddress).port])
        //之后由receiver负责接收上传的文件,上传完成后回调uploadFinished
    }
    
    /**
     * 若上传成功，将临时文件从临时目录移动到存储目录
     * 备份文件：同FileClient，向备份StorageNode上传文件及uuid
     * 向FileServer发送文件信息，表示已成功存储文件
     *      
     */
    void uploadFinished(Map info){
        //todo:失败处理,根据info 的result,error
        //todo:更新容量
        File file = new File(fileDir,info.uuid.toString())
        Files.move(info.tempFile.toPath(),file.toPath())
        NodeInfo backup=info.backupNodeInfo
        if(backup){
            //it is main
            //重复FileClient代码，将backupNodeInfo设为null,
            def freePort= Util.freePort
            def nodeClient=new Client(backup.address,backup.port,ConnectionType.TCP,{Client c,ChannelHandlerContext ctx->
                c.request=[action:'upload',senderPort:freePort,fileName:file.name,fileSize:file.size(),backupNodeInfo:null,uuid:info.uuid]
            })
            def nodeResponse = nodeClient.response
            if(!nodeResponse.result){
                println nodeResponse.message
                return
            }
            def sender=new FileSender(backup.address,nodeResponse.receiverPort,freePort)
            sender.send(file)
            logger.info "正在备份文件 $nodeResponse.uuid 至备份结点 $backup"
        }
        client.request=[action:'addFile',uuid:info.uuid,nodeInfo:nodeInfo,fileName:info.fileName,fileSize:info.fileSize]
    }
    
    
    
    def remove(ChannelHandlerContext ctx,Map map){
        new File(dataDir,"$map.uuid").delete()
    }
    
}
