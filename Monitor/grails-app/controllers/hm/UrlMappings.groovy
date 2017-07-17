package hm

class UrlMappings {
    
    static mappings = {
        "/$controller/$action?"{
            /*constraints {
                
            }*/
        }
    
        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
