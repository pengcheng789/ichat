package top.pengcheng789.ichat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 *
 */
public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private int port;

    public Server() {
        this(8090);
    }

    public Server(int port) {
        this.port = port;
    }

    public void enable() {
        LOGGER.info("Server startup ...");
        this.service();
    }

    private void service(){
        ServerSocketChannel serverSocketChannel = getServerSocketChannel();

        Selector selector = getSelector();
        try {
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            LOGGER.error("Register server socket channel failure.");
            throw new RuntimeException(e);
        }

        ByteBuffer reciveBuffer = ByteBuffer.allocate(1024*1024);
        ByteBuffer sendBuffer = ByteBuffer.allocate(1024*1024);

        Runnable getSystemInputTask = () -> {
            sendBuffer.clear();
            sendBuffer.put(new Scanner(System.in).nextLine().getBytes());
            sendBuffer.flip();
        };
        new Thread(getSystemInputTask).start();

        while (true) {
            try {
                selector.select();
            } catch (IOException e) {
                LOGGER.error("Selector select failure.");
                throw new RuntimeException(e);
            }

            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> it = keySet.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if (key.isAcceptable()) {
                    accept(selector, serverSocketChannel);
                } else if (key.isReadable()) {
                    read(key, reciveBuffer);
                } else if (key.isWritable()) {
                    write(key, sendBuffer);
                }
            }
        }

    }

    private ServerSocketChannel getServerSocketChannel() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            LOGGER.info("Server is listening *:8090 ...");
            return serverSocketChannel;
        } catch (IOException e) {
            LOGGER.error("Create server socket channel failure.");
            throw new RuntimeException(e);
        }
    }

    private Selector getSelector() {
        try {
            return Selector.open();
        } catch (IOException e) {
            LOGGER.error("Create selector failure.");
            throw new RuntimeException(e);
        }
    }

    private void accept(Selector selector, ServerSocketChannel serverSocketChannel) {
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            LOGGER.info("Accept a socket from " + socketChannel.getRemoteAddress());
        } catch (IOException e) {
            LOGGER.error("Accept socket failure.");
            throw new RuntimeException(e);
        }
    }

    private void read(SelectionKey key, ByteBuffer receiveBuffer) {
        SocketChannel socketChannel = (SocketChannel)key.channel();

        receiveBuffer.clear();
        try {
            socketChannel.read(receiveBuffer);
        } catch (IOException e) {
            LOGGER.error("Receive data failure.");
            throw new RuntimeException(e);
        }

        receiveBuffer.flip();
        String message = new String(receiveBuffer.array());
        LOGGER.info("Received: " + message);
        System.out.println("Received: " + message);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void write(SelectionKey key, ByteBuffer sendBuffer) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            socketChannel.write(sendBuffer);
            String message = new String(sendBuffer.array());
            LOGGER.info("Send: " + message);
            System.out.println("Send: " + message);
        } catch (IOException e) {
            LOGGER.error("Send data failure.");
            throw new RuntimeException(e);
        }
        key.interestOps(SelectionKey.OP_READ);
    }
}
