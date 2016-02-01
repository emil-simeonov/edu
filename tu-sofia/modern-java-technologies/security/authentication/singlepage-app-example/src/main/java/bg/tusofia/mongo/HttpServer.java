package bg.tusofia.mongo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
            MongoClient dbClient = createDatabaseClient(vertx);
            // We can now query auth provider about authentication and authorization details stored in Mongo DB.
            MongoAuth authProvider = MongoAuth.create(dbClient, new JsonObject());
            AuthHandler redirectAuthHandler = RedirectAuthHandler.create(authProvider, "loginpage.html");
            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create());
            router.route().handler(CookieHandler.create());
            router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
            router.route().handler(UserSessionHandler.create(authProvider));
            // index.html in this web app will require authentication with valid user
            router.route("/").handler(redirectAuthHandler);
            router.route("/index.html").handler(redirectAuthHandler);
            // Login requests will be handled using form authentication and will redirect to "index.html", when successful.
            FormLoginHandler formLoginHandler = FormLoginHandler.create(authProvider);
            formLoginHandler.setDirectLoggedInOKURL("index.html");
            router.route("/login").handler(formLoginHandler);
            // This is how we create new users (working with forms).
            router.post("/create-user").handler(ctx -> {
                MultiMap form = ctx.request().formAttributes();
                String username = form.get("username");
                String password = form.get("password");
                List<String> roles = new LinkedList<>();
                roles.add("user");
                if ("on".equalsIgnoreCase(form.get("hasAdminRole"))) {
                    roles.add("admin");
                }
                authProvider.insertUser(username, password, roles, new ArrayList<>(), id -> {
                    if (id.succeeded()) {
                        authProvider.authenticate(new JsonObject().put("username", username).put("password", password), user -> {
                            if (user.succeeded()) {
                                ctx.setUser(user.result());
                                redirectTo(ctx, "index.html");
                            } else {
                                redirectTo(ctx, "loginpage.html");
                            }
                        });
                    } else {
                        redirectTo(ctx, "loginpage.html");
                    }
                });
            });
            router.post("/logout").handler(ctx -> executeIfUserLoggedIn(ctx, c -> {
                // And this is how we could log out at any point.
                ctx.clearUser();
                redirectTo(ctx, "loginpage.html");
            }));

            router.get("/api/authors").handler(ctx -> dbClient.find("authors", new JsonObject(), res -> {
                if (res.failed()) {
                    ctx.fail(res.cause());
                } else {
                    // We will only serve this request if there is an authenticated user.
                    executeIfUserLoggedIn(ctx, c -> {
                        // In addition we would like to allow this operation only for admins
                        executeIfHasRole(ctx, "admin", res.result(), data -> jsonResponse(ctx, data));
                    });
                }
            }));
            router.get("/api/books").handler(ctx -> dbClient.find("books", new JsonObject(), booksResult -> {
                if (booksResult.failed()) {
                    ctx.fail(booksResult.cause());
                } else {
                    executeIfUserLoggedIn(ctx, c -> {
                        JsonArray books = new JsonArray(booksResult.result());
                        books.forEach(current -> {
                            JsonObject book = (JsonObject) current;
                            JsonObject authorAsJson = book.getJsonObject("author");
                            JsonObject query = new JsonObject().put("_id", authorAsJson.getValue("_id"));
                            dbClient.findOne("authors", query, new JsonObject(), authorResult -> {
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
                    });
                }
            }));
            // A RESTful method for adding a new book
            router.post("/api/books").handler(ctx -> executeIfUserLoggedIn(ctx, c -> {
                JsonObject newBook = ctx.getBodyAsJson();
                dbClient.save("books", newBook, res -> {
                    if (res.failed()) {
                        ctx.fail(res.cause());
                    } else {
                        newBook.put("author", new JsonObject().put("_id", newBook.getJsonObject("author").getValue("_id")));
                        ctx.response().setStatusCode(201);
                        jsonResponse(ctx, newBook);
                    }
                });
            }));
            // Creating the StaticHandler instance in this way binds the "webroot/index.html" to the application root path
            router.route().handler(StaticHandler.create());
            // We need to feed the SSL configuration during starting up our HTTP server.
            HttpServerOptions httpServerOptions = new HttpServerOptions().setSsl(true).setKeyStoreOptions(
                    new JksOptions().setPath("ssl/sslkeystore.jks").setPassword("Abcd1234")
            );
            vertx.createHttpServer(httpServerOptions).requestHandler(router::accept).listen(4443);
        }

        /**
         * Executes some business logic only if the currently signed user has the passed role assigned.
         *
         * @param ctx      routing context
         * @param roleName name of the requested role
         * @param data     the data that will be processed further by the authorization callback
         * @param cb       authorization callback
         */
        private void executeIfHasRole(RoutingContext ctx, String roleName, List<JsonObject> data, Handler<JsonArray> cb) {
            ctx.user().isAuthorised(MongoAuth.ROLE_PREFIX + roleName, r -> {
                if (r.succeeded()) {
                    if (r.result()) {
                        cb.handle(new JsonArray(data));
                    } else {
                        ctx.response().setStatusCode(401).end();
                    }
                } else {
                    ctx.response().setStatusCode(401).end();
                }
            });
        }

        /**
         * Executes the authenticated callback if there is an authenticated user. If not, returns 403.
         *
         * @param ctx             routing context to be used for sending out a HTTP response
         * @param authenticatedCb a callback to invoke in case of successful authentication
         */
        private void executeIfUserLoggedIn(RoutingContext ctx, Handler<RoutingContext> authenticatedCb) {
            if (ctx.user() != null) {
                authenticatedCb.handle(ctx);
            } else {
                ctx.response().setStatusCode(403).end();
            }
        }

        /**
         * Redirects to a given static page.
         *
         * @param ctx  the routing context used for sending out responses
         * @param page the static page to be loaded
         */
        private void redirectTo(RoutingContext ctx, String page) {
            ctx.response().setStatusCode(301).putHeader("Location", page).end();
        }
    }
}
