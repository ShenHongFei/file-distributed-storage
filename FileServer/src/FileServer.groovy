class FileServer{
    void service(){
        def ss = new ServerSocket(6600)
        ss.accept{
            println it.inputStream.text
        }
    }
    
    static void main(String... args){
        
    }
}