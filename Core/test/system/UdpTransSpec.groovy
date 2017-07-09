package system

import io.netty.channel.ChannelHandlerContext
import spock.lang.Shared
import spock.lang.Specification

import java.awt.*

class UdpTransSpec extends Specification{
    
    @Shared TestServer testServer =new TestServer()
    @Shared Client     client     =new Client('localhost',8081,ConnectionType.UDP,null)
    
    class TestServer{
        Server server=new Server(this,8081,ConnectionType.UDP,null)
        def data
        ChannelHandlerContext ctx
        String action

        def defaultMethod(String name,Object[] args) {
            action=name
            ctx=args[0]
            data=args[1]
        }
    }
    
    def '测试发送,接收 UDP Request'(){
        when:
        client.request=[action:'test']
        sleep(500)
        then:
        testServer.action=='test'
        testServer.data==[action:'test']
    }
    
}
