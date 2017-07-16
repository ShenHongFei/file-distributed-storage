package system

import spock.lang.Specification

class FileSenderSpec extends Specification{
    def '启动并发送一个文件'(){
        expect:
//        def start=System.currentTimeMillis()
        new FileSender('localhost',7080).with{
            send(new File('data/灰色鲨鱼.png'))
            send(new File('data/灰色鲨鱼.png'))
        }
        
//        println((System.currentTimeMillis()-start)/1000)
    }
}
