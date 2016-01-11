package bg.tusofia.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

/**
 * This class illustrates the HTTP basics.
 * </p>
 * The Vert.x-web module is used in order to facilitate the implementation of a "Hello World" asynchronous client and
 * server. The client fires a GET HTTP request to the server running at http://localhost:8080/. The server parses
 * the request and prepares a HTTP response to be send back to the client. The corresponding handler sets the value of
 * "content-type" header, so that a web browser would render a HTML page. The body of the response is also filled out.
 * </p>
 * Once the client receives a response, it dumps in the console the HTTP status of the response, its headers as well as
 * the body of the response.
 * </p>
 * We have chosen to use vert.x as the underlying technology, so that most of the basic HTTP support in Vert.x is
 * illustrated. Keep in mind that working with HTTP using another technology stack is mostly done in the very same way.
 */
public class HttpHelloWorld {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new HelloWorldServer());
        vertx.deployVerticle(new HelloWorldClient());
    }

    /**
     * A simple HTTP protocol helper, which illustrates some of the facilities for processing HTTP requests in Vert.x.
     * These are very similar to other JAVA frameworks and frameworks implemented in other languages.
     */
    private static class HttpHelper {
        private HttpHelper() {
        }

        /**
         * Builds a string representation of the HTTP response status.
         *
         * @param httpResponse http response to be send back to the browser
         * @return a formatted string consisting of the HTTP status code and message.
         */
        private static String httpStatus(HttpClientResponse httpResponse) {
            return String.format("Status: %d, %s", httpResponse.statusCode(), httpResponse.statusMessage());
        }

        /**
         * Builds a string representation of any given header entry as part of a HTTP request or response.
         *
         * @param header a map entry containing key and value for the corresponding HTTP header
         * @return a formatted string consisting of the header key and value
         */
        private static String httpHeader(Map.Entry<String, String> header) {
            return String.format("Header [%s: %s]", header.getKey(), header.getValue());
        }

        /**
         * Returns the body of HTTP requests/responses out of a buffer that is usually part of these entities.
         *
         * @param bodyBuffer a vert.x buffer containing the representation of HTTP requests and responses
         * @return a formatted string representation of the request/response body
         */
        private static String body(Buffer bodyBuffer) {
            return String.format("Body: %s", bodyBuffer.getString(0, bodyBuffer.length()));
        }

        /**
         * Returns the HTTP method of an incoming HTTP request. To be used server-side only.
         *
         * @param request an incoming HTTP request
         * @return a string representation of the HTTP method for the passed request
         */
        private static String httpMethod(HttpServerRequest request) {
            return String.format("HTTP Method: %s", request.method().name());
        }

        /**
         * Sends a HTTP response to a client. The response is formatted as HTML. The body of the response is passed as
         * an argument.
         *
         * @param response the HTTP response that we need to build and send back to the client
         * @param body     the HTTP response body that we would like to wrap as part of the response
         */
        private static void createAndSendResponse(HttpServerResponse response, String body) {
            response.putHeader("content-type", "text/html").end(body);
        }
    }

    /**
     * This class represents a very simple, almost-echo, web server (TCP server
     * listening for and handling HTTP requests). It does illustrate what we could do in order to
     * process an arbitrary HTTP request.
     */
    private static class HelloWorldServer extends AbstractVerticle {
        /**
         * Handles HTTP requests that are part of the passed instance of <code>RoutingContext</code>.
         *
         * @param ctx Routing context that wraps the request and response objects
         */
        private static void handleHttpRequest(RoutingContext ctx) {
            dumpHttpRequest(ctx.request());
            HttpHelper.createAndSendResponse(ctx.response(), "Hello World!");
        }

        /**
         * Does basic parsing of an incoming HTTP request and dumps its HTTP method, headers and body (if the used HTTP
         * method supports body transports) in the console.
         *
         * @param request the incoming HTTP request
         */
        private static void dumpHttpRequest(HttpServerRequest request) {
            System.out.println();
            System.out.println("The following HTTP request was accepted by the server:");
            // We iterate through all headers and dump them in the console
            request.headers().forEach(header -> System.out.println(HttpHelper.httpHeader(header)));
            HttpMethod httpMethod = request.method();
            // Depending on the HTTP method, we will either parse the body of the request and dump it in console or not.
            System.out.println(HttpHelper.httpMethod(request));
            if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT) {
                // The request body is dumped in the console
                request.bodyHandler(in -> System.out.println(HttpHelper.body(in)));
            }
        }

        @Override
        public void start() throws Exception {
            Router router = Router.router(vertx);
            // Handles all incoming HTTP requests independent of their path (i.e. without real routing)
            router.route().handler(HelloWorldServer::handleHttpRequest);
            // The created router instance is passed as a request handler, i.e. its apply instance method
            vertx.createHttpServer().requestHandler(router::accept).listen(HttpConstants.DEFAULT_HTTP_PORT);
            System.out.println("Echo HTTP server is up & running.");
        }
    }

    private static class HelloWorldClient extends AbstractVerticle {
        @Override
        public void start() throws Exception {
            /* This is how client requests are constructed and sent to the corresponding HTTP server.
             The used HTTP method is "GET". The vert.x API also supports all of the HTTP methods through its 'post',
             "put", etc. methods.
            */
            vertx.createHttpClient().getNow(
                    HttpConstants.DEFAULT_HTTP_PORT,
                    HttpConstants.LOCALHOST,
                    HttpConstants.INDEX,
                    (httpResponse) -> {
                        System.out.println();
                        System.out.println("The following HTTP response was delivered to the client:");
                        System.out.println(HttpHelper.httpStatus(httpResponse));
                        // We iterate through all headers and dump them in the console
                        httpResponse.headers().forEach(header -> System.out.println(HttpHelper.httpHeader(header)));
                        // The response body is also dumped in the console
                        httpResponse.bodyHandler(in -> System.out.println(HttpHelper.body(in)));
                    });
        }
    }
}
