package top.pengcheng789.ichat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8090));

            ByteBuffer receiveBuffer = ByteBuffer.allocate(32);
            ByteBuffer sendBuffer = ByteBuffer.allocate(32);

            String sendMessage = "";
            while (!sendMessage.equals("bye")) {
                socketChannel.read(receiveBuffer);
                receiveBuffer.flip();
                System.out.print("Receive message: ");
                while (receiveBuffer.hasRemaining()) {
                    System.out.print((char)receiveBuffer.get());
                }
                System.out.println();
                receiveBuffer.clear();

                System.out.print("Type message: ");
                sendMessage = new Scanner(System.in).nextLine();
                sendBuffer.clear();
                sendBuffer.put(sendMessage.getBytes());
                sendBuffer.flip();
                socketChannel.write(sendBuffer);
                //System.out.println("Send message: " + sendMessage);
            }
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
