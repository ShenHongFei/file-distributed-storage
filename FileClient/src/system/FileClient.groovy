package system

import io.netty.buffer.Unpooled
import io.netty.channel.socket.DatagramPacket
import io.netty.util.CharsetUtil
import io.netty.util.internal.SocketUtils

class FileClient{
    
    Scanner scanner =new Scanner(System.in)
    Client  client
    
    static void main(String... args){
        new FileClient().run(args)
    }
    
    
    FileClient(){
        client=new Client('127.0.0.1',8080,ConnectionType.UDP,null)
    }
    
    def run(String... args){
        if(!args){
            println '''
            上传文件
            java -jar FileClient.jar upload afile
            该程序输出一个新存储文件的uuid，就是在服务器端保存的文件的唯一标识。
            
            下载文件
            java -jar FileClient.jar download uuid
            
            删除文件
            java -jar FileClient.jar remove uuid
            '''.stripMargin()
            return
        }
        this.metaClass.getMetaMethod(args[0]).invoke(this,args.length>1?args[1..-1]:null)
        client.waitClose()
    }
    
    def upload(){

        client.request=[aaa:'bbb',action:'test']
//        new File('tt.jpg').bytes=client.response.attachment
//        client.channel.close()
    }
    
    def download(){
        
    }
    
    def remove(){
        
    }
}
