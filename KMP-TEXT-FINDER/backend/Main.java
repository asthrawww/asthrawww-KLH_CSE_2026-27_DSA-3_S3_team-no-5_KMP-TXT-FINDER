import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {

    // -----------------------------
    // KMP - Build LPS Array
    // -----------------------------
    public static int[] buildLPS(String pattern) {

        int[] lps = new int[pattern.length()];

        int length = 0;
        int i = 1;

        while (i < pattern.length()) {

            if (pattern.charAt(i) == pattern.charAt(length)) {

                length++;
                lps[i] = length;
                i++;

            } else {

                if (length != 0) {

                    length = lps[length - 1];

                } else {

                    lps[i] = 0;
                    i++;
                }
            }
        }

        return lps;
    }


    // -----------------------------
    // KMP Search
    // -----------------------------
    public static List<Integer> kmpSearch(
            String text,
            String pattern) {

        List<Integer> positions = new ArrayList<>();

        if (pattern.isEmpty()) {
            return positions;
        }

        int[] lps = buildLPS(pattern);

        int i = 0;
        int j = 0;

        while (i < text.length()) {

            if (text.charAt(i) == pattern.charAt(j)) {

                i++;
                j++;
            }

            if (j == pattern.length()) {

                positions.add(i - j);

                j = lps[j - 1];

            } else if (
                    i < text.length()
                    && text.charAt(i) != pattern.charAt(j)
            ) {

                if (j != 0) {

                    j = lps[j - 1];

                } else {

                    i++;
                }
            }
        }

        return positions;
    }


    // -----------------------------
    // Read POST data
    // -----------------------------
    public static String getParameter(
            String data,
            String parameter) {

        String[] pairs = data.split("&");

        for (String pair : pairs) {

            String[] keyValue = pair.split("=", 2);

            if (keyValue.length == 2 &&
                    keyValue[0].equals(parameter)) {

                return URLDecoder.decode(
                        keyValue[1],
                        StandardCharsets.UTF_8
                );
            }
        }

        return "";
    }


    // -----------------------------
    // Main Server
    // -----------------------------
    public static void main(String[] args)
            throws Exception {

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(8080),
                        0
                );

        server.createContext(
                "/search",
                Main::handleSearch
        );

        server.start();

        System.out.println(
                "KMP Backend is running!"
        );

        System.out.println(
                "http://localhost:8080"
        );
    }


    // -----------------------------
    // Search API
    // -----------------------------
    public static void handleSearch(
            HttpExchange exchange)
            throws IOException {

        // CORS
        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Origin",
                "*"
        );

        if (exchange.getRequestMethod().equals("OPTIONS")) {

            exchange.getResponseHeaders().add(
                    "Access-Control-Allow-Methods",
                    "POST, OPTIONS"
            );

            exchange.getResponseHeaders().add(
                    "Access-Control-Allow-Headers",
                    "Content-Type"
            );

            exchange.sendResponseHeaders(204, -1);

            return;
        }


        if (!exchange.getRequestMethod().equals("POST")) {

            sendResponse(
                    exchange,
                    "{\"error\":\"POST request required\"}"
            );

            return;
        }


        // Read request body
        InputStream input =
                exchange.getRequestBody();

        String data =
                new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );


        String text =
                getParameter(data, "text");

        String pattern =
                getParameter(data, "pattern");


        if (text.isEmpty() ||
                pattern.isEmpty()) {

            sendResponse(
                    exchange,
                    "{\"error\":\"Enter text and pattern\"}"
            );

            return;
        }


        // Measure KMP execution time
        long start =
                System.nanoTime();


        List<Integer> positions =
                kmpSearch(text, pattern);


        long end =
                System.nanoTime();


        double time =
                (end - start) / 1_000_000.0;


        int[] lps =
                buildLPS(pattern);


        // Create JSON response
        StringBuilder json =
                new StringBuilder();


        json.append("{");

        json.append("\"occurrences\":")
                .append(positions.size())
                .append(",");


        json.append("\"positions\":[");
        
        for (int i = 0;
             i < positions.size();
             i++) {

            json.append(positions.get(i));

            if (i < positions.size() - 1) {
                json.append(",");
            }
        }

        json.append("],");


        json.append("\"lps\":[");

        for (int i = 0;
             i < lps.length;
             i++) {

            json.append(lps[i]);

            if (i < lps.length - 1) {
                json.append(",");
            }
        }

        json.append("],");


        json.append("\"time\":")
                .append(time)
                .append(",");


        json.append("\"textLength\":")
                .append(text.length())
                .append(",");


        json.append("\"patternLength\":")
                .append(pattern.length());


        json.append("}");


        sendResponse(
                exchange,
                json.toString()
        );
    }


    // -----------------------------
    // Send response
    // -----------------------------
    public static void sendResponse(
            HttpExchange exchange,
            String response)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json"
        );

        byte[] bytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.sendResponseHeaders(
                200,
                bytes.length
        );

        OutputStream output =
                exchange.getResponseBody();

        output.write(bytes);

        output.close();
    }
}
