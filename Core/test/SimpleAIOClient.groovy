import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.charset.Charset

ByteBuffer buf = ByteBuffer.allocate(1024)
AsynchronousSocketChannel channel = AsynchronousSocketChannel.open()
channel.connect(new InetSocketAddress('localhost',7080)).get()
channel.read(buf).get()
println new String(buf.array(),Charset.forName('utf-8'))