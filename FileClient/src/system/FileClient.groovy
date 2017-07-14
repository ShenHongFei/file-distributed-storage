package system

import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import util.Util

class FileClient{
    
    static logger=LogManager.logger
    
    Scanner scanner =new Scanner(System.in)
    Properties config =new Properties()
    Client  client
    Map<String,UUID> files=[:]
    File fileSer
    
    static void main(String... args){
        new FileClient(args[0]).run(*args[1..-1])
    }
    
    
    FileClient(String configLocation){
        config.load(new FileInputStream(configLocation))
        client=new Client(config.fileServerIP,config.fileServerPort as Integer,ConnectionType.TCP,null)
        fileSer=new File('data/files.ser')
        if(fileSer.exists()){
            fileSer.withObjectInputStream{
                files=it.readObject()
            }
        }
    }
    
    def run(String... args){
        if(!args){
            println '''
            上传文件
            java -jar FileClient.jar 配置文件 upload afile
            该程序输出一个新存储文件的uuid，就是在服务器端保存的文件的唯一标识。
            
            下载文件
            java -jar FileClient.jar 配置文件 download uuid
            
            删除文件
            java -jar FileClient.jar 配置文件 remove uuid
            '''.stripMargin()
            return
        }
        this.metaClass.getMetaMethod(args[0]).invoke(this,args[1])
    }
    
    /**
     * 向FileServer获取存储结点信息
     * 建立到StorageNode的连接并发送上传文件请求(用于上传文件的端口号，文件大小，文件名)
     * @StorageNode 生成 uuid，发回响应（FileReceiver的端口号），给FileReceiver设置临时文件（记录下inetAddress和临时文件的映射）
     * new FileSender(),上传文件
     * 等待FileSender发送完成 同时 输出进度信息
     * @StorageNode 向FileServer发送文件信息{文件名，uuid}
     * @FileServer 存储文件信息，向StorageNode确认
     * @StorageNode 移动临时文件到存储文件夹
     */
    void upload(String path){
        client.request=[action:'selectNode']
        //todo:FileServer失去响应后阻塞？超时时间
        def resp = client.response
        if(!resp.result){
            println resp.message
            return 
        }
        NodeInfo nodeInfo= resp.nodeInfo
        def file = new File(path)
        def freeport= Util.freePort
        def nodeclient=new Client(nodeInfo.address,nodeInfo.port,ConnectionType.TCP,{Client c,ChannelHandlerContext ctx->
            c.request=[action:'uploadInit',senderPort:freeport,fileName:file.name,fileSize:file.size()]
        })
        def nodeResponse = nodeclient.response
        if(!nodeResponse.result){
            println nodeResponse.message
            return
        }
        def sender=new FileSender(nodeInfo.address,nodeResponse.receiverPort)
        sender.send(file)
        files[file.name]=nodeResponse.uuid
        println nodeResponse.uuid
        saveFiles()
    }
    
    void download(String uuidstr){
        def uuid = UUID.fromString(uuidstr)
        client.request=[action:'getFileInfo',uuid:uuid]
        def resp = client.response
        if(!resp.result) return
        FileInfo fileInfo=resp.fileinfo
        NodeInfo main=fileInfo.main
        NodeInfo backup=fileInfo.backup
        def resp2=null
        try{
            def mainclient=new Client(main.address,main.port,ConnectionType.TCP,null)
            mainclient.request=[action:'download',uuid: uuid]
            def trymain = new Thread({resp2=mainclient.response})
            trymain.start()
            trymain.join(10*1000)
        }catch(any){}
        
        if(!resp2){
            def backupclient=new Client(backup.address,backup.port,ConnectionType.TCP,null)
            backupclient.request=[action:'download',uuid: uuid]
            resp2=backupclient.response
        }
        if(resp2){
            new File("data/$fileInfo.name").bytes=resp2.file
        }else{
            println '下载失败'
        }
    }
    
    void remove(String uuidstr){
        def uuid = UUID.fromString(uuidstr)
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
    
    
    
}
