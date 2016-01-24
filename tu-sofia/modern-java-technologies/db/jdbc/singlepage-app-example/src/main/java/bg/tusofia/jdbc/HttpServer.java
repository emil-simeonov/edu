package bg.tusofia.jdbc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.List;

/**
 * This class illustrates how a Single Page Application (SPA) could be built on top of vert.x.
 * It handles static resources (/index.html) and also exposes simple RESTful API consumed by the JavaScript client
 * application.
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

        private static JDBCClient createDatabaseClient(Vertx vertx) {
            return JDBCClient.createNonShared(vertx, new JsonObject()
                    .put("url", "jdbc:postgresql://localhost:5432/bookstore")
                    .put("user", "postgres")
                    .put("password", "Abcd1234")
                    .put("driver_class", "org.postgresql.Driver"));
        }

        private static void jdbcStatement(JDBCClient client, Handler<SQLConnection> executor, Handler<AsyncResult<SQLConnection>> errorHandler) {
            client.getConnection(res -> {
                if (res.failed()) {
                    if (errorHandler != null) {
                        errorHandler.handle(res);
                    }
                } else if (res.succeeded()) {
                    executor.handle(res.result());
                }
            });
        }

        private static JsonObject createBook(JsonArray record) {
            JsonObject book = new JsonObject();
            book.put("id", record.getValue(0));
            book.put("isbn", record.getValue(1));
            book.put("name", record.getValue(2));
            book.put("author", createAuthor(record.getInteger(3), record.getString(4), record.getString(5)));
            return book;
        }

        private static JsonObject createAuthor(int id, String firstName, String familyName) {
            JsonObject author = new JsonObject();
            author.put("id", id);
            author.put("firstName", firstName);
            author.put("familyName", familyName);
            return author;
        }

        @Override
        public void start() throws Exception {
            JDBCClient jdbcClient = createDatabaseClient(vertx);
            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create());
            router.get("/api/authors").handler(ctx -> jdbcStatement(jdbcClient, conn -> {
                String query = "select id, first_name, family_name from authors";
                conn.query(query, result -> {
                    if (result.failed()) {
                        ctx.fail(result.cause());
                    } else {
                        JsonArray authors = new JsonArray();
                        ResultSet rset = result.result();
                        rset.getResults().forEach(current -> authors.add(createAuthor(current.getInteger(0), current.getString(1), current.getString(2))));
                        jsonResponse(ctx, authors);
                    }
                }).close();
            }, res -> ctx.fail(res.cause())));
            router.get("/api/books").handler(ctx -> jdbcStatement(jdbcClient, conn -> {
                String query = "select books.id, isbn, name, author_id, authors.first_name, authors.family_name from books, authors where author_id = authors.id";
                conn.query(query, result -> {
                    if (result.failed()) {
                        ctx.fail(result.cause());
                    } else {
                        ResultSet rset = result.result();
                        JsonArray books = new JsonArray();
                        rset.getResults().forEach(current -> books.add(createBook(current)));
                        jsonResponse(ctx, books);
                    }
                }).close();
            }, res -> ctx.fail(res.cause())));
            router.get("/api/books/:id").handler(ctx -> jdbcStatement(jdbcClient, conn -> {
                String query = "select id, isbn, name, author_id from books where books.id = ?";
                int bookId = Integer.parseInt(ctx.request().getParam("id"));
                conn.queryWithParams(query, new JsonArray().add(bookId), result -> {
                    if (result.failed()) {
                        ctx.fail(result.cause());
                    } else {
                        ResultSet rset = result.result();
                        List<JsonArray> records = rset.getResults();
                        records.forEach(current -> jsonResponse(ctx, createBook(current)));
                    }
                }).close();
            }, res -> ctx.fail(res.cause())));
            // A RESTful method for adding a new book
            router.post("/api/books").handler(ctx -> {
                JsonObject newBook = ctx.getBodyAsJson();
                jdbcStatement(jdbcClient, conn -> {
                    String st = "insert into books (isbn, name, author_id) values (?, ?, ?)";
                    JsonArray params = new JsonArray().add(newBook.getString("isbn")).add(newBook.getString("name")).add(newBook.getJsonObject("author").getInteger("id"));
                    conn.updateWithParams(st, params, result -> {
                        if (result.failed()) {
                            ctx.fail(result.cause());
                        } else {
                            ctx.response().setStatusCode(201);
                            jsonResponse(ctx, newBook);
                        }
                    }).close();
                }, res -> ctx.fail(res.cause()));
            });
            // Creating the StaticHandler instance in this way binds the "webroot/index.html" to the application root path
            router.route().handler(StaticHandler.create());
            vertx.createHttpServer().requestHandler(router::accept).listen(8080);
        }
    }
}
