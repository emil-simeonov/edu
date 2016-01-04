package bg.tusofia;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Set;

public class NonBlockingTCPServer implements ITCPServer {
    private int port;

    public NonBlockingTCPServer(int port) {
        this.port = port;
    }

    @Override
    public void startServer() throws IOException {
        Charset charset = Charset.forName("UTF-8");
        CharsetEncoder encoder = charset.newEncoder();
        CharsetDecoder decoder = charset.newDecoder();
        ByteBuffer buffer = ByteBuffer.allocate(512);
        Selector selector = Selector.open();
        ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(port));
        server.configureBlocking(false);
        SelectionKey serverKey = server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Non-blocking sockets server started.");
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                server.close();
                return;
            }
            if (selector.select() == 0) {
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            for (Iterator<SelectionKey> keysIter = keys.iterator(); keysIter.hasNext(); ) {
                SelectionKey key = keysIter.next();
                keysIter.remove();
                if (key == serverKey && key.isAcceptable()) {
                    try (SocketChannel client = server.accept()) {
                        sendResponse(client, encoder, obtainInput(decoder, buffer, key, client));
                    }
                }
            }
        }
    }

    private void sendResponse(SocketChannel client, CharsetEncoder encoder, String input) throws IOException {
        if (input != null) {
            String msg = "Non-blocking TCP Server... " + input;
            client.write(encodeMessage(encoder, msg));
        }
    }

    private ByteBuffer encodeMessage(CharsetEncoder encoder, String msg) throws CharacterCodingException {
        return encoder.encode(CharBuffer.wrap(msg));
    }

    private String obtainInput(CharsetDecoder decoder, ByteBuffer buffer, SelectionKey key, SocketChannel client) throws IOException {
        try {
            int numberOfBytesRead = client.read(buffer);
            if (numberOfBytesRead == -1) {
                key.cancel();
                return null;
            }
            // change the mode of the buffer, so it is now available for "reading"
            buffer.flip();
            String msg = decoder.decode(buffer).toString();
            return msg.trim();
        } finally {
            // clear the buffer, so that it could be written to (e.g. for sending out a response to a TCP client
            buffer.clear();
        }
    }
}
