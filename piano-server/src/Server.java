import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Zefeng Zhang (zzhang@rsvp.ai)
 * @since 2020-05-09
 */
public class Server {
  static int port = 50005;
  static int bufferSize = 10000000;

  public static void main(String args[]) throws Exception {

    DatagramSocket serverSocket = new DatagramSocket(null);
    serverSocket.setReuseAddress(true);
    serverSocket.bind(new InetSocketAddress(port));
    serverSocket.setReceiveBufferSize(10000000);

    System.out.println("Creating the buffer to hold the received data of size " + bufferSize + "...");
    byte[] receiveData = new byte[bufferSize];

    for (int i = 0; ; i++) {
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      serverSocket.receive(receivePacket);
      String s = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
      String n = "1/" + i + ".dat";
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Files.write(Paths.get(n), s.getBytes());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }).start();
      System.out.println("received " + i);
//      System.out.println(s);
    }

//    serverSocket.close();
  }
}