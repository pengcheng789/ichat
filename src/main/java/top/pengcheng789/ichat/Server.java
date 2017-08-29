package top.pengcheng789.ichat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Scanner;

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

//        Selector selector = getSelector();
//        try {
//            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
//        } catch (ClosedChannelException e) {
//            LOGGER.error("Register server socket channel failure.");
//            throw new RuntimeException(e);
//        }

        ByteBuffer receiveBuffer = ByteBuffer.allocate(1024*1024);
        ByteBuffer sendBuffer = ByteBuffer.allocate(1024*1024);
        sendBuffer.put("Connect success.".getBytes());
        sendBuffer.flip();

        SocketChannel socketChannel;
        try {
            socketChannel = serverSocketChannel.accept();
        } catch (IOException e) {
            LOGGER.error("Accept socket failure.");
            throw new RuntimeException(e);
        }

        try {
            socketChannel.write(sendBuffer);
        } catch (IOException e) {
            LOGGER.error("Send ok failure.");
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                socketChannel.read(receiveBuffer);
                receiveBuffer.flip();

                StringBuilder message = new StringBuilder("");
                while (receiveBuffer.hasRemaining()) {
                   message.append((char)receiveBuffer.get());
                }
                LOGGER.info("Receive: " + message.toString());
                //System.out.println("Receive: " + message.toString());
                receiveBuffer.clear();

                if (message.toString().equals("bye")) break;
            } catch (IOException e) {
                LOGGER.error("Receive message failure.");
                throw new RuntimeException(e);
            }

            System.out.print("Type message: ");
            sendBuffer.clear();
            String sendMessage = new Scanner(System.in).nextLine();
            sendBuffer.put(sendMessage.getBytes());
            try {
                sendBuffer.flip();
                socketChannel.write(sendBuffer);
                LOGGER.info("Send: " + sendMessage);
            } catch (IOException e) {
                LOGGER.error("Send message failure");
                throw new RuntimeException(e);
            }
        }

        try {
            serverSocketChannel.close();
            LOGGER.info("Connection close.");
        } catch (IOException e) {
            LOGGER.error("Close server socket channel failure.");
            throw new RuntimeException(e);
        }

    }

    private ServerSocketChannel getServerSocketChannel() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            //serverSocketChannel.configureBlocking(false);
            LOGGER.info("Server is listening *:8090 ...");
            return serverSocketChannel;
        } catch (IOException e) {
            LOGGER.error("Create server socket channel failure.");
            throw new RuntimeException(e);
        }
    }
}
