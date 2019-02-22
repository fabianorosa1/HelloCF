package io.simonis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HelloCF {
  
  static class Handler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      InputStream is = exchange.getRequestBody();
      while (is.read(new byte[512]) != -1); // 'is' will be closed implicitly when we close 'os'
      System.out.println(exchange.getRemoteAddress() + " -> " + exchange.getLocalAddress() + exchange.getRequestURI() +
          " (" + exchange.getProtocol() + ")");
      StringBuffer response = new StringBuffer();
      response.append("Environment:\n");
      System.getenv().forEach((k, v) -> { response.append(k + " = " + v + "\n"); });
      response.append("\nProperties:\n");
      System.getProperties().forEach((k, v) -> { response.append(k + " = " + v + "\n"); });
      exchange.sendResponseHeaders(200, response.length());
      OutputStream os = exchange.getResponseBody();
      os.write(response.toString().getBytes());
      os.close();      
    }
  }

  public static void main(String[] args) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    server.createContext("/", new Handler());
    server.start();
  }
}
