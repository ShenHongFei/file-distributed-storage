package system

import core.FileReceiver
import spock.lang.Specification

class FileReceiverSpec extends Specification{
    def '启动并接收一个文件'(){
        expect:
        new FileReceiver(7080,new File('data/temp.png')).waitClose()
    }
}