package system

import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager

class FileClient{
    
    static logger=LogManager.logger
    
    Scanner scanner =new Scanner(System.in)
    Properties config =new Properties()
    Client  client
    Map<String,UUID> files=[:]
    
    static void main(String... args){
        new FileClient(args[0]).run(*args[1..-1])
    }
    
    
    FileClient(String configLocation){
        config.load(new FileInputStream(configLocation))
        client=new Client(config.FileServerIP,config.FileServerPort as Integer,ConnectionType.TCP,null)
        File fileser=new File('data/files.ser')
        if(fileser.exists()){
            fileser.withObjectInputStream{
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
    
    def upload(String path){
        client.request=[action:'selectNode']
        def resp = client.response
        if(!resp.result){
            println resp.message
            return 
        }
        NodeInfo nodeinfo= resp.nodeinfo
        def file = new File(path)
        def nodeclient=new Client(nodeinfo.address,nodeinfo.port,ConnectionType.TCP,{Client c,ChannelHandlerContext ctx->
            c.request=[action:'upload',name:file.name,file:file.bytes]
        })
        def map = nodeclient.response
        if(!map.result){
            println map.message
            return
        }
        files[file.name]=map.uuid
        println map.uuid
        saveFiles()
    }
    
    def download(String uuidstr){
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
    
    def remove(String uuidstr){
        def uuid = UUID.fromString(uuidstr)
        client.request=[action:'remove',uuid:uuid]
        files.removeAll{k,v->v==uuid}
        saveFiles()
        println '删除成功'
    }
    
    def saveFiles(){
        File fileser=new File('data/files.ser')
        fileser.withObjectOutputStream{
            it.writeObject(files)
        }
    }
}
