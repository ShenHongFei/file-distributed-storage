package system

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.util.CharsetUtil
import io.netty.util.internal.SocketUtils

class FileClient{
    
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
        def nodeclient=new Client(main.address,main.port,ConnectionType.TCP,{Client c,ChannelHandlerContext ct->
            c.request=[action:'download',uuid: uuid]
        })
        resp2=nodeclient.response
        new File("data/$fileInfo.name").bytes=resp2.file
    }
    
    def remove(){
        
    }
}
