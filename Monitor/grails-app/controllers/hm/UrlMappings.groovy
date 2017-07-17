package hm

class UrlMappings {
    
    static mappings = {
        "/"(controller: 'monitor',action:'index')
        "500"(view:'/error')
        "404"(view:'/notFound')
        '/**'(controller:'resource',action:'get')
    }
}
