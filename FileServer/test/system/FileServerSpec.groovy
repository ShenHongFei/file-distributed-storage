package system

import spock.lang.Specification

class FileServerSpec extends Specification{
    
    def '运行服务器'(){
        expect:
        new FileServer().run()
    }
}
