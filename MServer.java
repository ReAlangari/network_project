package mserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class MServer {

    ServerSocket ser_sock;
    public static List<MultiClientsAdapter> existed_clients;

    public MServer() throws IOException {
        existed_clients = new ArrayList<MultiClientsAdapter>();
        ser_sock = new ServerSocket(5554);
        while (true) {
            Socket client_sock = ser_sock.accept();
            MultiClientsAdapter clientAdapter = new MultiClientsAdapter(client_sock, this);
            clientAdapter.start();
            existed_clients.add(clientAdapter);
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Server is running and waiting...");
        System.out.println("*****************************");
        new MServer();

    }

}

class MultiClientsAdapter extends Thread {

    PrintWriter out;
    BufferedReader in;
    String thisClient;
    Socket client_sock;
    MServer mserver;

    public MultiClientsAdapter(Socket client_sock, MServer mserver) {
        this.client_sock = client_sock;
        this.mserver = mserver;
    }
    public void run() {
        try {
            out = new PrintWriter(client_sock.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(client_sock.getInputStream()));
            while (true) {

                try {
                    String clientMessage = in.readLine();
                    if (clientMessage.contains("connectReq")) {
                        String[] requestSpliter = clientMessage.split(">");
                        thisClient = requestSpliter[1];
                        sendExistedClients();
                        System.out.println("Hello and Welcome " + thisClient);
                        System.out.println("**Existed Clients are sent to " + thisClient);
                    
                    }else if (clientMessage.contains("all")) {
                        List<MultiClientsAdapter> temp_list = mserver.existed_clients;
                        String message_to_all = "all>";
                        String message = clientMessage.split(">")[1];
                        System.out.println("**message to all clients is sent from " + thisClient);
                        for (MultiClientsAdapter item : temp_list) {
                            message_to_all = message_to_all + thisClient + ">" + message;
                            item.out.println(message_to_all);
                            message_to_all = "all>";
                        }
                    } else if (clientMessage.contains("secrete")) {
                        List<MultiClientsAdapter> temp_list = mserver.existed_clients;
                        String secrete_message = "secrete>";
                        String destination = clientMessage.split(">")[1];
                        String sent_message = clientMessage.split(">")[2];
                        for (MultiClientsAdapter item : temp_list) {
                            if (item.thisClient.equals(destination)) {
                                secrete_message = secrete_message + sent_message + ">" + thisClient;
                                item.out.println(secrete_message);
                                System.out.println("**secrete message from " + thisClient + " to " + item.thisClient);
                                break;
                            }
                        }
                    }
                    if (clientMessage.contains("bye")) {
                        System.out.println("**Bye " + thisClient);
                        client_sock.close();
                        MServer.existed_clients.remove(this);
                        sendExistedClients();
                        break;

                    }
                } catch (IOException ex) {
                    Logger.getLogger(MultiClientsAdapter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MultiClientsAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }

       }
    

    public void sendExistedClients() {
        String exislted_clients_res = "existedClient>";
        List<MultiClientsAdapter> temp_list = mserver.existed_clients;
        for (MultiClientsAdapter item : temp_list) {
            String name = item.thisClient;
            exislted_clients_res = exislted_clients_res + name + ">";
        }
        for (MultiClientsAdapter item : temp_list) {
            item.out.println(exislted_clients_res);
        }
    }

}
