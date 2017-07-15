package system

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import util.Util

import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FileClient{
    
    static final logger=LogManager.logger
    
    Scanner scanner =new Scanner(System.in)
    Properties config =new Properties()
    Client  client
    Map<String,UUID> files=[:]
    File fileSer
    File dataDir
    File tempDir
    File fileDir
    
    static void main(String... args){
        new FileClient(args[0]).run(*args[1..-1])
    }
    
    
    FileClient(String configLocation){
        config.load(new FileInputStream(configLocation))
        (dataDir=new File("data/$config.fileClientName")).mkdirs()
        client=new Client(config.fileServerIP,config.fileServerPort as Integer,ConnectionType.TCP,null)
        fileSer=new File(dataDir,'files.ser')
        (tempDir=new File(dataDir,'temp')).mkdirs()
        (fileDir=new File(dataDir,'file')).mkdirs()
        if(fileSer.exists()){
            fileSer.withObjectInputStream{
                files=it.readObject()
            }
        }
    }
    
    void run(String... args){
        if(!args){
            println('''
            上传文件
            java -jar FileClient.jar 配置文件 upload afile
            该程序输出一个新存储文件的uuid，就是在服务器端保存的文件的唯一标识。
            
            下载文件
            java -jar FileClient.jar 配置文件 download uuid
            
            删除文件
            java -jar FileClient.jar 配置文件 remove uuid
            '''.stripMargin(' '))
            return
        }
        this.metaClass.getMetaMethod(args[0]).invoke(this,args[1])
    }
    
    /**
     * 向FileServer获取存储结点信息
     * 建立到StorageNode的连接并发送上传文件请求(用于上传文件的端口号，文件大小，文件名,backupNodeInfo)
     * @StorageNode 生成 uuid，发回响应（FileReceiver的端口号），给FileReceiver设置临时文件（记录下inetAddress和临时文件的映射）
     * new FileSender(),上传文件
     * 等待FileSender发送完成 同时 输出进度信息
     * @StorageNode 向FileServer发送文件信息{文件名，uuid}
     * @FileServer 存储文件信息，向StorageNode确认
     * @StorageNode 移动临时文件到存储文件夹
     */
    void upload(String path){
        ChannelFuture clientClose
        ChannelFuture senderClose
        ChannelFuture nodeClientClose
        try{
            client.request=[action:'selectNode']
            def resp = client.response
            clientClose = client.channel.close()
            NodeInfo main=resp.mainNodeInfo
            NodeInfo backup=resp.backupNodeInfo
            if(!resp.mainNodeInfo){
                println '当前无可用存储结点'
                logger.warn '当前无可用存储结点'
                return
            }
            def file = new File(path)
            def freePort= Util.freePort
            def nodeClient=new Client(main.address,main.port,ConnectionType.TCP,{Client c,ChannelHandlerContext ctx->
                c.request=[action:'upload',senderPort:freePort,fileName:file.name,fileSize:file.size(),backupNodeInfo:backup]
            })
            def nodeResponse = nodeClient.response
            nodeClientClose=nodeClient.channel.close()
            if(!nodeResponse.result){
                println nodeResponse.message
                return
            }
            def sender=new FileSender(new InetSocketAddress(main.address,nodeResponse.receiverPort),freePort)
            sender.send(file).sync()
            senderClose=sender.channel.close()
            files[file.name]=nodeResponse.uuid
            println "上传成功 文件名：${file.name}, UUID:$nodeResponse.uuid"
            saveFiles()
        }finally{
            clientClose?.sync()
            senderClose?.sync()
            nodeClientClose?.sync()
        }
    }
    
    /**
     * 向FileServer获取主要、备份StorageNode信息
     * 新建FileReceiver，向主要StorageNode发送 {端口号，uuid}下载文件
     * 主结点失败，尝试备份结点
     * @param uuidstr
     */
    synchronized void download(String uuidstr){
        def uuid = UUID.fromString(uuidstr)
        client.request=[action:'getFileInfo',uuid:uuid]
        def resp = client.response
        if(!resp.result) return
        FileInfo fileInfo=resp.fileinfo
        NodeInfo main=fileInfo.main
        NodeInfo backup=fileInfo.backup
        Boolean result=false
        def tryNode={ NodeInfo node->
            FileReceiver receiver=null
            Client nodeClient=null
            try{
                def tryMain = new Thread({
                    logger.info "尝试连接结点 $node.address $node.port"
                    nodeClient=new Client(node.address,node.port,ConnectionType.TCP,null)
                    receiver=new FileReceiver(0)
                    nodeClient.request=[action:'downloadInit',uuid: uuid]
                    def nodeResponse=nodeClient.response
                    if(!nodeResponse?.result){
                        return 
                    }
                    receiver.infos[new InetSocketAddress(node.address,nodeResponse.senderPort)]=[fileName:fileInfo.name,fileSize:fileInfo.size,tempFile:new File(tempDir,fileInfo.uuid.toString()),uuid:fileInfo.uuid,finishedCallback:this.&downloadFinished]
                    nodeClient.request=[action:'download',uuid:fileInfo.uuid,receiverPort:(receiver.serverChannel.localAddress() as InetSocketAddress).port]
                    result=true
                })
                tryMain.start()
                tryMain.join(10*1000)
            }catch(any){
                logger.warn '结点连接失败'
                any.printStackTrace()
            }finally{
                nodeClient?.channel?.close()
                if(!result){
                    receiver?.serverChannel?.close()
                }
            }
        }
        if(!main||!main.aliveNow){
            logger.info '无主结点或主结点不在线，尝试备份结点'
            if(!backup){
                logger.error '无备份结点'
                println '下载失败'
                client.channel.close().sync()
                return
            }else if(!backup.aliveNow){
                logger.error '备份结点不在线'
                println '下载失败'
                client.channel.close().sync()
                return
            }
        }
        if(main&&main.aliveNow) tryNode(main)
        if(!result){
            logger.warn '主结点连接失败，尝试备份结点'
            if(backup&&backup.aliveNow) tryNode(backup)
            if(!result) {
                logger.error '备份结点连接失败，下载失败'
                client.channel.close().sync()
                return
            }
        }
        //todo:输出下载进度
        wait()
        client.channel.close().sync()
    }
    
    synchronized void downloadFinished(Map info){
        def tempFile=info.tempFile
        if(tempFile.size()!=info.fileSize){
            println '下载失败'
            notifyAll()
        }else{
            Files.move(tempFile.toPath(),new File(fileDir,info.uuid.toString()).toPath(),StandardCopyOption.REPLACE_EXISTING)
        }
        println("下载成功 文件 $info.fileName 大小 ${Util.getHumanReadableByteCount(info.fileSize,false)} UUID=$info.uuid")
        notifyAll()
    }
    
    
    void remove(String uuidstr){
        def uuid = UUID.fromString(uuidstr)
        logger.info "删除文件：${files.find{k,v->v==uuid}}"
        client.request=[action:'remove',uuid:uuid]
        files.removeAll{k,v->v==uuid}
        saveFiles()
        println '删除成功'
    }
    
    void saveFiles(){
        fileSer.withObjectOutputStream{
            it.writeObject(files)
        }
    }
    
    void waitCommand(t){
        client.channel.closeFuture().sync()
    }
    
    /**
     * 显示本地文件及uuid
     */
    void displayLocalFiles(){
        //todo @yyt
    }
    
}
