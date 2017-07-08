package system

import spock.lang.Specification


class FileClientTest extends Specification{
    def 'run'(){
        def client = new FileClient()
        
        expect:
        client.run('upload')
    }
}
