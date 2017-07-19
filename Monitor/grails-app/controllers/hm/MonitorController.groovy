package hm

import system.Client

class MonitorController{
    //todo:想明白为什么在这些controller中能够直接得到params,controller,action,为什么不能调用removeAll等方法
    
    static responseFormats = ['gsp']
    Client client=new Client(Application.config.fileServerIP,Application.config.fileServerPort as Integer)

    def index(){
        client.request=[action:'getStatus']
        def response = client.response
        render(view:'/index.gsp',model:response)
    }
    
}