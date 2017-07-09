package system

import spock.lang.Specification


class FileClientTest extends Specification{
    
    def '上传文件'(){
        def fileClient = new FileClient('cfg/FileServer.properties')
        
        expect:
        fileClient.run('upload','data/灰色鲨鱼.png')
    }
}
