class FileClient{
    static void main(String... args){
        def socket = new Socket('127.0.0.1',6600)
        def stream = new DataOutputStream(socket.outputStream)
        stream.writeUTF('hello')
        stream.flush()
        stream.close()
    }
}