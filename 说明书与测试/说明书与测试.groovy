import spock.lang.Shared
import spock.lang.Specification
import system.FileClient
import system.FileServer
import system.StorageNode

class 说明书与测试 extends Specification{
    
    @Shared FileServer fileServer
    @Shared StorageNode storageNode1
    @Shared StorageNode storageNode2
    @Shared StorageNode storageNode3
    @Shared StorageNode storageNode4
    
    def '运行服务器'(){
        fileServer=new FileServer()
        when:
        fileServer.run()
        then:
        noExceptionThrown()
    }
    
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
    def '运行StorageNode3'(){
        storageNode3=new StorageNode('cfg/StorageNode/StorageNode3.properties')
        when:
        storageNode3.run()
        then:
        noExceptionThrown()
    }
    def '运行StorageNode4'(){
        storageNode4=new StorageNode('cfg/StorageNode/StorageNode4.properties')
        when:
        storageNode4.run()
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
    
    def 'FileClient上传文件2'(){
        def fileClient=new FileClient('cfg/FileClient/FileClient2.properties')
        when:
        fileClient.run('upload','data/test-file/灰色鲨鱼.png')
        then:
        noExceptionThrown()
    }
    
    def 'FileClient上传大文件，有进度'(){
        def fileClient=new FileClient('cfg/FileClient/FileClient1.properties')
        when:
        fileClient.run('upload','data/test-file/image.iso')
        then:
        noExceptionThrown()
    }
    
    def 'FileClient下载最近上传的文件'(){
        def fileClient=new FileClient('cfg/FileClient/FileClient1.properties')
        when:
        fileClient.run('download',fileClient.files.iterator().next().value.toString())
        then:
        noExceptionThrown()
    }
    
    def 'FileClient根据UUID下载文件'(){
        def fileClient=new FileClient('cfg/FileClient/FileClient1.properties')
        when:
        fileClient.run('download','e5781c93-b81f-4e55-bc53-f5b890f7bc74')
        then:
        noExceptionThrown()
    }
    
    def 'FileClient删除最近上传文件'(){
        def fileClient=new FileClient('cfg/FileClient/FileClient1.properties')
        when:
        fileClient.run('remove',fileClient.files.iterator().next().value.toString())
        then:
        noExceptionThrown()
    }
    
    def 'FileClient删除文件'(){
        def fileClient=new FileClient('cfg/FileClient/FileClient1.properties')
        when:
        fileClient.run('remove',fileClient.files.iterator().next().value.toString())
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
