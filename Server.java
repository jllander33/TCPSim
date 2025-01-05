import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static final double DROP_PROBABILITY = 0.2;

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running...");

            try (Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                String message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sollicitudin sollicitudin iaculis. Maecenas euismod erat quis imperdiet aliquam. Maecenas turpis felis, molestie in nibh euismod, pulvinar sollicitudin dolor. Etiam libero lectus, euismod vel interdum vitae, rhoncus non urna. Nulla efficitur nisl nec finibus lacinia. In convallis, elit eget tincidunt molestie, nulla nisi fermentum risus, sit amet varius purus ipsum id quam. Duis placerat nisl et sem blandit commodo. Curabitur consequat tempus velit sit amet vestibulum. Donec non est ultricies, tempus nulla rhoncus, blandit tortor. Suspendisse potenti. Cras sit amet dignissim ligula (end of lorem ipsum)";
                                      
                String[] packets = createPackets(message);
                Random random = new Random();

                for (String packet : packets) {
                    Double randomValue = random.nextDouble();
                    System.out.println("Random Value: " + randomValue);
                    if (randomValue > DROP_PROBABILITY) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        out.println(packet);
                    }
                }
                System.out.println("All packets sent");
                int numPackets = packets.length;
                String lastPacket = "SEQ:-1|DATA:" + numPackets;
                System.out.println("Last Packet: " + lastPacket);
                out.println(lastPacket);

                String request;
                while ((request = in.readLine()) != null) {
                    System.out.println("Received Request: " + request);
                    if (request.startsWith("REQ:")) {
                        int packetIndex = Integer.parseInt(request.substring(4));
                        if (packetIndex >= 0 && packetIndex < packets.length) {
                            Double randomValue = random.nextDouble();
                            System.out.println("Random Value for REQ: " + randomValue);
                            if (randomValue > DROP_PROBABILITY) {
                                out.println(packets[packetIndex]);
                            }
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private static String[] createPackets(String message) {
        int packetSize = 20;
        int totalPackets = (int) Math.ceil((double) message.length() / packetSize);
        String[] packets = new String[totalPackets];

        for (int i = 0; i < totalPackets; i++) {
            int start = i * packetSize;
            int end = Math.min(start + packetSize, message.length());
            String fragment = message.substring(start, end);
            packets[i] = "SEQ:" + i + "|DATA:" + fragment;
        }
        return packets;
    }
}