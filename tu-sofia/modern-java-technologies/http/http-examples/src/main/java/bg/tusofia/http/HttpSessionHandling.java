package bg.tusofia.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class HttpSessionHandling extends AbstractVerticle {
    public static void main(String[] argv) {
        Vertx.vertx().deployVerticle(new HttpSessionHandling());
    }

    private static int incrementHits(Session session) {
        Integer hits = session.get(HttpConstants.SESSION_KEY);
        hits = hits == null ? 1 : (hits + 1);
        session.put(HttpConstants.SESSION_KEY, hits);
        return hits;
    }

    private static String createBody(int hits) {
        return String.format("<html><body><h1>Hit Count: %d</h1></body></html>", hits);
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        router.route().handler(ctx -> {
            Session session = ctx.session();
            int hits = incrementHits(session);
            ctx.response().putHeader("content-type", "text/html").end(createBody(hits));
        });
        vertx.createHttpServer().requestHandler(router::accept).listen(HttpConstants.DEFAULT_HTTP_PORT);
    }
}
