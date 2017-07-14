import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.Future

//Java 异步编程
//ExecutorService pool = Executors.newFixedThreadPool(2)
//def task=new FutureTask({1+2})
//pool.execute(task)
//println task.isDone()
//println task.get()
//pool.shutdown()

// SimpleAIOServer
AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(7080))
while(true){
    Future<AsynchronousSocketChannel> future = serverChannel.accept()
    def channel = future.get()
    channel.write(ByteBuffer.wrap('hello'.bytes))
}