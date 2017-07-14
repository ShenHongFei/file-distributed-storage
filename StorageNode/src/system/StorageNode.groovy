package system

import io.netty.channel.ChannelHandlerContext

class StorageNode{
    
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
     * synchronized 保证空间分配正常
     */
    synchronized void uploadInit(ChannelHandlerContext ctx,Map map){
        def uuid=UUID.randomUUID()
        //todo:检查剩余空间
        receiver.infos[new InetSocketAddress((ctx.channel().remoteAddress() as InetSocketAddress).address,map.senderPort)]=[fileName:map.fileName,fileSize:map.fileSize,uuid:uuid,tempFile:new File(tempDir,uuid.toString())]
        ctx.channel().writeAndFlush([result:true,uuid:uuid,receiverPort:(receiver.serverChannel.localAddress() as InetSocketAddress).port])
    }
    
    def upload(ChannelHandlerContext ctx,Map map){
        //todo:检查容量，同步代码
        def uuid=UUID.randomUUID()
        new File("$config.RootFolder/$uuid").bytes=map.file
        println 'in upload'
        ctx.writeAndFlush([result:true,message:'上传成功，正在备份',uuid:uuid])
        //todo:更新容量
        client.request=[action:'addFile',uuid:uuid,nodeInfo:nodeInfo,name:map.name,size:map.file.length]
        def resp = client.response
        //todo:无备份结点的处理
        if(!resp.result) return 
        def nodeclient=new Client(resp.nodeInfo.address,resp.nodeInfo.port,ConnectionType.TCP,{Client c,ChannelHandlerContext ct->
            ct.writeAndFlush([action:'backup',uuid: uuid,file:map.file])
        })
        nodeclient.response
    }
    
/*    def backup(ChannelHandlerContext ctx,Map map){
        new File("$config.rootFolder/$map.uuid").bytes=map.file
        client.request=[action:'addBackup',nodeInfo:nodeInfo,uuid:map.uuid]
    }*/
    
/*    def download(ChannelHandlerContext ctx,Map map){
        ctx.writeAndFlush([result:true,file:new File("$config.rootFolder/$map.uuid").bytes])
    }*/
    
    def remove(ChannelHandlerContext ctx,Map map){
        new File(dataDir,"$map.uuid").delete()
    }
    
}
