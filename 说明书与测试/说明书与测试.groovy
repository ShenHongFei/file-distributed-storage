import spock.lang.Shared
import spock.lang.Specification
import system.FileClient
import system.FileServer
import system.StorageNode

class 说明书与测试 extends Specification{
    
    @Shared FileServer fileServer
    @Shared StorageNode storageNode1
    @Shared StorageNode storageNode2
    
    def '运行StorageNode1'(){
        storageNode1=new StorageNode('cfg/StorageNode/StorageNode1.properties')
        when:
        storageNode1.run()
        then:
        noExceptionThrown()
    }
    
    def '运行StorageNode2'(){
        storageNode2=new StorageNode('cfg/StorageNode/StorageNode2.properties')
        when:
        storageNode2.run()
        then:
        noExceptionThrown()
    }
    
    def '运行服务器'(){
        fileServer=new FileServer()
        when:
        fileServer.run()
        then:
        noExceptionThrown()
    }
    
    
    def 'FileClient上传文件'(){
        def fileClient=new FileClient('cfg/FileClient/FileClient1.properties')
        when:
        fileClient.run('upload','data/test-file/灰色鲨鱼.png')
        then:
        noExceptionThrown()
    }
    
    def '清空所有数据'(){
        
    }
    
/*    def 'FileClient下载文件'(){
        when:
        def fileClient.run('download',fileClient.files.iterator().next().value.toString())
        then:
        noExceptionThrown()
    }
    
    def 'FileClient删除文件'(){
        def fileClient = new FileClient('cfg/FileServer.properties')
        when:
        fileClient.run('remove',fileClient.files.iterator().next().value.toString())
        then:
        noExceptionThrown()
    }*/
    
}
