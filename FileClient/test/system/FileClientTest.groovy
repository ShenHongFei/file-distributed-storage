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
        fileClient.run('download',fileClient.files.iterator().next().value.toString())
    }
    
}
