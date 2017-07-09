package system

import io.netty.channel.ChannelHandlerContext
import spock.lang.Shared
import spock.lang.Specification

import java.awt.Point

class TcpTransSpec extends Specification{
    
    @Shared TestServer testServer=new TestServer()
    @Shared Client client=new Client('localhost',8080,ConnectionType.TCP,{ChannelHandlerContext ctx,Client client->client.request=[action:'initConnect',aaa:'bbb',point:[new Point(1,2)]]})
    
    class TestServer{
        Server server=new Server(this,8080,ConnectionType.TCP,null)
        def data
        ChannelHandlerContext ctx
        String action

        def defaultMethod(String name,Object[] args) {
            action=name
            ctx=args[0]
            data=args[1]
        }
    }
    
    def '连接建立'(){
        sleep(1000)
        expect:
        testServer.action=='initConnect'
        testServer.data==[action:'initConnect',aaa:'bbb',point:[new Point(1,2)]]
        
    }
    
    def '测试发送并接收包含文件的 Request 和 Response'(){
        when:
        client.request=[action:'test',attachment:new File('data/'+'灰色鲨鱼.png').bytes]
        sleep(500)
        then:
        testServer.action=='test'
        testServer.data.attachment==new File('data/'+'灰色鲨鱼.png').bytes
    }
    
    def '大文件测试'(){
        when:
        client.request=[action:'test',attachment:new File('data/'+'灰色鲨鱼.png').bytes]
        sleep(500)
        then:
        testServer.action=='test'
        testServer.data.attachment==new File('data/'+'灰色鲨鱼.png').bytes
        
        when:
        //大文件测试
        testServer.ctx.writeAndFlush([result:true,attachment:new File('data/'+'image.iso').bytes])
        def map = client.response
        then:
        map.result
        map.attachment.length==new File('data/'+'image.iso').size()
    }
    
}
