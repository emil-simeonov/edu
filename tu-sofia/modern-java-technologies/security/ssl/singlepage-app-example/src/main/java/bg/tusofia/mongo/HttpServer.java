package bg.tusofia.mongo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

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

        private static MongoClient createDatabaseClient(Vertx vertx) {
            return MongoClient.createNonShared(vertx, new JsonObject().put("connection_string", "mongodb://localhost:27017").put("db_name", "bookstore"));
        }

        @Override
        public void start() throws Exception {
            MongoClient mongoClient = createDatabaseClient(vertx);
            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create());
            router.get("/api/authors").handler(ctx -> mongoClient.find("authors", new JsonObject(), res -> {
                if (res.failed()) {
                    ctx.fail(res.cause());
                } else {
                    jsonResponse(ctx, new JsonArray(res.result()));
                }
            }));
            router.get("/api/books").handler(ctx -> mongoClient.find("books", new JsonObject(), booksResult -> {
                if (booksResult.failed()) {
                    ctx.fail(booksResult.cause());
                } else {
                    JsonArray books = new JsonArray(booksResult.result());
                    books.forEach(current -> {
                        JsonObject book = (JsonObject) current;
                        JsonObject authorAsJson = book.getJsonObject("author");
                        JsonObject query = new JsonObject().put("_id", authorAsJson.getValue("_id"));
                        mongoClient.findOne("authors", query, new JsonObject(), authorResult -> {
                            if (authorResult.failed()) {
                                ctx.fail(authorResult.cause());
                            } else {
                                JsonObject result = authorResult.result();
                                authorAsJson.put("firstName", result.getValue("firstName"));
                                authorAsJson.put("familyName", result.getValue("familyName"));
                            }
                        });
                    });
                    jsonResponse(ctx, books);
                }
            }));
            // A RESTful method for adding a new book
            router.post("/api/books").handler(ctx -> {
                JsonObject newBook = ctx.getBodyAsJson();
                mongoClient.save("books", newBook, res -> {
                    if (res.failed()) {
                        ctx.fail(res.cause());
                    } else {
                        newBook.put("author", new JsonObject().put("_id", newBook.getJsonObject("author").getValue("_id")));
                        ctx.response().setStatusCode(201);
                        jsonResponse(ctx, newBook);
                    }
                });
            });
            // Creating the StaticHandler instance in this way binds the "webroot/index.html" to the application root path
            router.route().handler(StaticHandler.create());
            // We need to feed the SSL configuration during starting up our HTTP server.
            HttpServerOptions httpServerOptions = new HttpServerOptions().setSsl(true).setKeyStoreOptions(
                    new JksOptions().setPath("ssl/sslkeystore.jks").setPassword("Abcd1234")
            );
            vertx.createHttpServer(httpServerOptions).requestHandler(router::accept).listen(4443);
        }
    }
}
