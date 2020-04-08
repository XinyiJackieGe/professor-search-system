import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

/** Main class to start server and operate search. */
public class InfoSystem {
  public static void main(String[] args) throws Exception {
    int port = 8500;
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    System.out.println("server started at " + port);
    server.createContext("/", new RootHandler());
    server.createContext("/search", new EchoPostHandler());
    server.setExecutor(null);
    server.start();
  }
}
