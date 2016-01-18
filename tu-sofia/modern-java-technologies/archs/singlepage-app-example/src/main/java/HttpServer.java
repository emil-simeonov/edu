import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * This class illustrates how a Single Page Application (SPA) could be built on top of vert.x.
 */
public class HttpServer {
    public static void main(String[] argv) {
        Vertx.vertx().deployVerticle(new SinglePageVertical());
    }

    private static class SinglePageVertical extends AbstractVerticle {
        private static void jsonResponse(RoutingContext ctx, JsonObject jsonObject) {
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(jsonObject.encode());
        }

        private static void jsonResponse(RoutingContext ctx, JsonArray jsonArray) {
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(jsonArray.encode());
        }

        @Override
        public void start() throws Exception {
            JsonArray names = new JsonArray();
            Router router = Router.router(vertx);
            // We need to add a body handler so that we could obtain request body in JSON format
            router.route().handler(BodyHandler.create());
            // A RESTful method returning the list of names
            router.get("/api/names").handler(ctx -> jsonResponse(ctx, names));
            // A RESTful method returning a given name entry based on its id (its index in the names JSONArray)
            router.get("/api/names/:id").handler(ctx -> {
                int id = Integer.parseInt(ctx.request().getParam("id"));
                jsonResponse(ctx, names.getJsonObject(id));
            });
            // A RESTful method adding a new name to the list
            router.post("/api/names").handler(ctx -> {
                JsonObject newName = ctx.getBodyAsJson();
                names.add(newName);
                jsonResponse(ctx, newName);
            });
            // Creating the StaticHandler instance in this way binds the "webroot/index.html" to the application root path
            router.route().handler(StaticHandler.create());
            vertx.createHttpServer().requestHandler(router::accept).listen(8080);
        }
    }
}
