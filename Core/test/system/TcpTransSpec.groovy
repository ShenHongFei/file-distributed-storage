package system

import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import org.spockframework.compiler.model.CleanupBlock
import spock.lang.Shared
import spock.lang.Specification

import java.awt.Point

class TestServer{
    static final logger=LogManager.getLogger(TcpTransSpec)
    
    Server                server = new Server(this,8080,ConnectionType.TCP,null)
    def                   data
    ChannelHandlerContext ctx
    String                action
    
    def defaultMethod(String name,Object[] args){
        action=name
        ctx=args[0]
        data=args[1]
    }
    
    def echo(ChannelHandlerContext ctx,Map map){
        logger.info 'processing and echoing back...'
        sleep(1000)
        ctx.writeAndFlush(map)
    }
}

class TcpTransSpec extends Specification{
    
    static final logger=LogManager.getLogger(TcpTransSpec)
    
    @Shared TestServer testServer
    @Shared Client client
    @Shared Client testInitClient
    
    def 'TestServer能够接收Request，返回Response'(){
        when:'启动 TestServer'
        testServer=new TestServer()
        testServer.server.waitClose()
        then:
        noExceptionThrown()
    }
    
    def 'Client能够发送Request,接收Response'(){
        when:'启动 Client'
        client=new Client('localhost',8080,ConnectionType.TCP,null)
        then:
        noExceptionThrown()
        when:'测试发送接收Request,Response'
        client.request=[action:'echo',aaa:'bbb',point:[new Point(1,2)]]
        then:
        client.response==[action:'echo',aaa:'bbb',point:[new Point(1,2)]]
        client.shutdown()
    }
    
    def 'Client可以设置连接建立后的Action'(){
        when:
        testInitClient=new Client('localhost',8080,ConnectionType.TCP,{Client client,ChannelHandlerContext ctx->
            sleep(1000)
            client.request=[action:'echo',aaa:'bbb',point:[new Point(1,2)]]
        })
        then:
        testInitClient.response==[action:'echo',aaa:'bbb',point:[new Point(1,2)]]
    }
}
