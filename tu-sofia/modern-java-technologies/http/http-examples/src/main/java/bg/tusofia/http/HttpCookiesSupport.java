package bg.tusofia.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;

public class HttpCookiesSupport extends AbstractVerticle {
    public static void main(String[] argv) {
        Vertx.vertx().deployVerticle(new HttpCookiesSupport());
    }

    private static String createBody(String cookieValue) {
        return cookieValue != null ? cookieValue : "No cookie yet. Fire another request and see what happens. :-)";
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        router.route().handler(CookieHandler.create());
        router.route().handler(ctx -> {
            String cookieValue = null;
            Cookie cookie = ctx.getCookie("my-cookie");
            if (cookie == null) {
                ctx.addCookie(Cookie.cookie("my-cookie", "ha-ha-ha"));
            } else {
                cookieValue = cookie.getValue();
            }
            ctx.response().putHeader("content-type", "text/html").end(createBody(cookieValue));
        });
        vertx.createHttpServer().requestHandler(router::accept).listen(HttpConstants.DEFAULT_HTTP_PORT);
    }
}
