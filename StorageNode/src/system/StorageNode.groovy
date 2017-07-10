package system

import io.netty.channel.ChannelHandlerContext

class StorageNode{
    
    Properties config =new Properties()
    Client     udpClient
    Client     client
    Server     server
    NodeInfo   nodeinfo
    
    static void main(String... args){
        new StorageNode(args[0]).run()
    }
    
    StorageNode(String configLocation){
        config.load(new FileInputStream(configLocation))
        nodeinfo=new NodeInfo(address:config.NodeIP,name:config.NodeName,port:config.NodePort as Integer)
        udpClient=new Client(config.FileServerIP,config.FileServerUdpPort as Integer,ConnectionType.UDP,null)
        client=new Client(config.FileServerIP,config.FileServerPort as Integer,ConnectionType.TCP,null)
        server=new Server(this,config.NodePort as Integer,ConnectionType.TCP,null)
    }
    
    def run(){
        while(true){
            nodeinfo.alive=new Date()
            udpClient.request=[action:'nodeReg',nodeinfo:nodeinfo]
            sleep(5000)
        }
    }
    
    def upload(ChannelHandlerContext ctx,Map map){
        //todo:检查容量，同步代码
        def uuid=UUID.randomUUID()
        new File("$config.RootFolder/$uuid").bytes=map.file
        println 'in upload'
        ctx.writeAndFlush([result:true,message:'上传成功，正在备份',uuid:uuid])
        //todo:更新容量
        client.request=[action:'addFile',uuid:uuid,nodeinfo:nodeinfo,name:map.name,size:map.file.length]
        def resp = client.response
        //todo:无备份结点的处理
        if(!resp.result) return 
        def nodeclient=new Client(resp.nodeinfo.address,resp.nodeinfo.port,ConnectionType.TCP,{Client c,ChannelHandlerContext ct->
            ct.writeAndFlush([action:'backup',uuid: uuid,file:map.file])
        })
        nodeclient.response
    }
    
    def backup(ChannelHandlerContext ctx,Map map){
        new File("$config.RootFolder/$map.uuid").bytes=map.file
        client.request=[action:'addBackup',nodeinfo:nodeinfo,uuid:map.uuid]
    }
    
    def download(ChannelHandlerContext ctx,Map map){
        ctx.writeAndFlush([result:true,file:new File("$config.RootFolder/$map.uuid").bytes])
    }
    
    def remove(ChannelHandlerContext ctx,Map map){
        new File("$config.RootFolder/$map.uuid").delete()
    }
    
}
