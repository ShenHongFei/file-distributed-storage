package system

import spock.lang.Specification


class FileClientTest extends Specification{
    
    def '上传文件'(){
        def fileClient = new FileClient('cfg/FileServer.properties')
        when:
        fileClient.run('upload','data/upload/灰色鲨鱼.png')
        then:
        noExceptionThrown()
    }
    
    def '下载文件'(){
        def fileClient = new FileClient('cfg/FileServer.properties')
        expect:
        fileClient.run('download','3fc1e208-c645-4724-bda6-de58368b0690')
        
    }
    
}
