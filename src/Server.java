import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private final Scanner CONSOLE = new Scanner(System.in);
    private final String host;
    private final int port;
    private Scanner in;
    private PrintWriter out;

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        String message;
        try (
                ServerSocket server = serverInit();
                Socket client = clientInit(server)
        ) {
            inputOutputStreamsInit(client);
            Thread serverOutputTread = new ServerOutputTread(out);
            serverOutputTread.start();

            while (true){
                message = receive();
                if (message.endsWith("exit")){
                    System.out.println("exit");
                    break;
                }
            }
            System.out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private ServerSocket serverInit() {
        System.out.println("server starting...");
        try {
            return new ServerSocket(port, 10, InetAddress.getByName(host));
        } catch (IOException e) {
            System.out.println("ошибка при инициализации серверного сокета");
            throw new RuntimeException(e);
        }
    }

    private Socket clientInit(ServerSocket server) {
        try {
            return server.accept();
        } catch (IOException e) {
            System.out.println("ошибка при подключении клиентского сокета");
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
            System.out.println("ошибка инициалиазации потока вывода");
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
            System.out.println("ошибка иницаилизации потока ввода");
            throw new RuntimeException(e);
        }
    }

    private String receive() {
        return in.nextLine();
    }

    private void sendMessage() {
        String message = CONSOLE.nextLine();
        send(message);
    }

    private void send(String message) {
        out.println("SERVER> " + message);
    }

    class ServerOutputTread extends Thread{
        private final Scanner CONSOLE = new Scanner(System.in);
        private final PrintWriter out;

        public ServerOutputTread(PrintWriter out){
            this.out = out;
            setDaemon(true);
        }
    }

    public static void main(String[] args) {
        Server server = new Server("127.0.0.1", 9000);
        server.start();
    }
}
