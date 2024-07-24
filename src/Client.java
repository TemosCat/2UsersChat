import java.io.*;
import java.net.Socket;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {
    private final Scanner CONSOLE = new Scanner(System.in);
    private final String host;
    private final int port;
    private Scanner in;
    private PrintWriter out;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        String message;
        try (Socket client = clientInit()) {
            inputOutputStreamsInit(client);

            Thread clientOutputThread = new ClientOutputThread(out);
            clientOutputThread.start();

            while (true) {
                message = receive();
                if (message.endsWith("exit")) {
                    send("exit");
                    break;
                }
                System.out.println(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Socket clientInit() {
        System.out.println("client starting...");
        try {
            return new Socket(host, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void inputOutputStreamsInit(Socket client) {
        in = inInit(client);
        out = outInit(client);
    }

    private PrintWriter outInit(Socket client) {
        try {
            return new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    client.getOutputStream()
                            )
                    ), true
            );
        } catch (IOException e) {
            System.out.println("ошибка инициализации потока вывода");
            throw new RuntimeException(e);
        }
    }

    private Scanner inInit(Socket client) {
        try {
            return new Scanner(
                    new InputStreamReader(
                            client.getInputStream()
                    )
            );
        } catch (IOException e) {
            System.out.println("ошибка инициализации потока ввода");
            throw new RuntimeException(e);
        }
    }

    private String receive() {
        try {
            return in.nextLine();
        } catch (NoSuchElementException e) {
            return "exit";
        }
    }

    private void sendMessage() {
        String message = CONSOLE.nextLine();
        send(message);
    }

    private void send(String message) {
        out.println("CLIENT> " + message);
    }


    static class ClientOutputThread extends Thread {
        private final Scanner CONSOLE = new Scanner(System.in);
        private final PrintWriter out;

        public ClientOutputThread(PrintWriter out) {
            this.out = out;
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                String message = CONSOLE.nextLine();
                out.println("CLIENT> " + message);
                if (message.equals("exit")) {
                    break;
                }
            }
            out.close();
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 9000);
        client.start();
    }
}