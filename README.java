 package org.s.server;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;

public class ServerSocket {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try {
            // Charger le keystore du serveur
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream keyStoreStream = ServerSocket.class.getResourceAsStream("/server-keystore.p12")) {
                keyStore.load(keyStoreStream, "changeit".toCharArray());
            }

            // Charger le truststore du serveur
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (InputStream trustStoreStream = ServerSocket.class.getResourceAsStream("/server-truststore.p12")) {
                trustStore.load(trustStoreStream, "changeit".toCharArray());
            }

            // Initialiser le KeyManager et le TrustManager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, "changeit".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);

            // Initialiser le contexte SSL
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            ServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
            try (SSLServerSocket serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(PORT)) {
                System.out.println("Server is running...");

                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());
                         BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream())) {

                        System.out.println("SSL connection established with client: " + clientSocket.getInetAddress());

                        BufferedReader in = new BufferedReader(new InputStreamReader(bis));
                        PrintWriter out = new PrintWriter(bos, true);

                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            System.out.println("Received: " + inputLine);
                            out.println("Echo: " + inputLine);
                            out.flush(); // Assurez-vous que les messages sont effectivement envoyés
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
