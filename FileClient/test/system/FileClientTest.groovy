package system

import spock.lang.Specification


class FileClientTest extends Specification{
    def 'upload'(){
        def client = new FileClient()
        
        expect:
        client.run('upload')
    }
}
