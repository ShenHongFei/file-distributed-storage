package system

import io.netty.channel.ChannelHandlerContext
import spock.lang.Shared
import spock.lang.Specification

class StorageNodeSpec extends Specification{
    
    class TestServer{
        Server                server = new Server(this,8081,ConnectionType.UDP,null)
        def                   data
        ChannelHandlerContext ctx
        String                action
        Map nodes=[:]
    
        def nodeReg(ChannelHandlerContext ctx,Map map){
            nodes.name=map.info
        }
    
        def defaultMethod(String name,Object[] args){
            action=name
            ctx=args[0]
            data=args[1]
        }
    }
    
    
    def '运行'(){
//        StorageNode.main('cfg/StorageNode.properties')
        TestServer testServer =new TestServer()
        def node = new StorageNode('cfg/StorageNode1.properties')
        
        when:
        def config= node.config
        then:
        println config
        when:
        new Thread({
            node.run()
        }).start()
        then:
        while(true){
            println(testServer.data)
            sleep(5000)
        }
        
    }
    
    def '运行 2'(){
//        StorageNode.main('cfg/StorageNode.properties')
        def node = new StorageNode('cfg/StorageNode2.properties')
        
        expect:
        new Thread({
            node.run()
        }).start()
        while(true){
            sleep(10000)
        }
    }
}
