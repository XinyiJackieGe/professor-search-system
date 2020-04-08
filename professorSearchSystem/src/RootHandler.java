import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** Root handler for root web UI. Query is input by text input. */
public class RootHandler implements HttpHandler {
  @Override
  public void handle(HttpExchange he) throws IOException {
    String response =
        "<!DOCTYPE html>\n"
            + "<html><body>\n"
            + "<h3 align=\"center\">Search Professors</h3>\n"
            + "<form method=\"Post\"\n"
            + "    action=\"search\">\n"
            + "    Your keywords: <input type=\"text\" name=\"query\" size=\"60\"><br>\n"
            + "    <input type=\"submit\" value=\"Search\"><br><br>\n"
            + "</form>\n"
            + "</body>\n"
            + "</html>";

    he.sendResponseHeaders(200, response.length());
    OutputStream os = he.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }
}
