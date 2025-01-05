import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private static final int PORT = 12345;
    private static final int TIMEOUT = 2000;

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            Map<Integer, String> receivedPackets = new TreeMap<>();
            String packet;
            int numPackets = -1;
            while ((packet = in.readLine()) != null) {
                System.out.println("Received Packet: " + packet);
                int seq = Integer.parseInt(packet.substring(packet.indexOf("SEQ:") + 4, packet.indexOf("|")).trim());
                String data = packet.substring(packet.indexOf("DATA:") + 5).trim();

                if (seq == -1) {
                    numPackets = Integer.parseInt(data);
                    break;
                }

                receivedPackets.put(seq, data);
            }

            System.out.println("All packets received");
            System.out.println("Number of packets: " + numPackets);
            System.out.println("Received Packets: " + receivedPackets.size());
            System.out.println("Missing Packets: " + (numPackets - receivedPackets.size()));

            ArrayList<Integer> missingPackets = new ArrayList<>();
            for (int i = 0; i < numPackets; i++) {
                if (!receivedPackets.containsKey(i)) {
                    missingPackets.add(i);
                    System.out.println("Missing Packet: " + i);
                }
            }
            
            while (receivedPackets.size() < numPackets) {
                Iterator<Integer> iterator = missingPackets.iterator();
                while (iterator.hasNext()) {
                    int missingSeq = iterator.next();
                    System.out.println("Requesting Missing Packet: " + missingSeq);
                    out.println("REQ:" + missingSeq);
                    long startTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startTime < TIMEOUT) {
                        if (in.ready()) {
                            String missingPacket = in.readLine();
                            if (missingPacket != null && missingPacket.startsWith("SEQ:")) {
                                System.out.println("Received Missing Packet: " + missingPacket);
                                int seq = Integer.parseInt(missingPacket.substring(missingPacket.indexOf("SEQ:") + 4, missingPacket.indexOf("|")).trim());
                                String data = missingPacket.substring(missingPacket.indexOf("DATA:") + 5).trim();
                                receivedPackets.put(seq, data);
                                iterator.remove();
                                break;
                            }
                        }
                    }
                }
            }
            
            
            StringBuilder message = new StringBuilder();
            for (String value : receivedPackets.values()) {
                System.out.println("Received Packet: " + value);
                message.append(value);
            }

            System.out.println("Received Message: " + message.toString().trim());
        }
    }
}