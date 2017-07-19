import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import spock.lang.Specification
import system.Client
import system.Server

class 简单ECHO模型 extends Specification{
    
    class EchoServer{
        Server server=new Server(this,6080)
        
        void echo(ChannelHandlerContext ctx,Map map){
            logger.info("执行echo函数，参数为"+map.toString())
            map.put('result',true)
            ctx.writeAndFlush(map)
        }
    }
    
    class EchoClient{
        Client client=new Client('127.0.0.1',6080)
        
        void sendRequest(){
            Map<String,Object> map=new HashMap<>()
            map.put("action","echo")
            map.put("someKey",new Date())
            
            //发送Map
            client.setRequest(map)
            
            //阻塞等待并接收Map
            Map<String,Object> response = client.getResponse()
            
            //输出日志
            logger.info(response)
            
            //释放连接
            client.channel.close().sync()
        }
    }
    
    def 'ECHO模型测试'(){
        when:
        new EchoServer()
        new EchoClient().sendRequest()
        then:
        noExceptionThrown()
    }
    
    
    
    static final logger=LogManager.getLogger(简单ECHO模型)
}
