package bg.tusofia.archs.mpa;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;

import java.io.File;

/**
 * This class represents a HTTP server providing server-side web page rendering through Handlebars4j.
 * In addition this HTTP server demonstrates how HTTP requests to the root application route ("/", "/index.html") could
 * be handled, as well as concrete resource paths (e.g. "/say-hi"). Last but not least, this example illustrates the
 * basics on HTML form processing.
 */
public class HttpServer {
    public static void main(String[] argv) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new TemplatesRenderingVertical());
    }

    /**
     * This is a vert.x vertical used for server-side Handlebars templates definition and rendering.
     */
    private static class TemplatesRenderingVertical extends AbstractVerticle {
        /**
         * Dynamically builds the resource path to each template in the "resources/templates" folder.
         *
         * @param templateName the name of the template
         * @return resource path for the passed template name
         */
        private static String templatePath(String templateName) {
            return HTTPConstants.TEMPLATES_DIR + File.separator + templateName + HTTPConstants.TEMPLATE_EXT;
        }

        /**
         * Renders Handlebars templates on the server side.
         * @param engine an instance of the Handlebars engine as part of the Handlebars template engine vert.x module.
         * @param ctx the routing context that is used to access the HTTP request and response as well as for passing
         *            passing values for template rendering.
         * @param templateName the name of the Handlebars template in the "resources/templates" folder
         */
        private static void render(HandlebarsTemplateEngine engine, RoutingContext ctx, String templateName) {
            engine.render(ctx, templatePath(templateName), res -> {
                if (res.succeeded()) {
                    ctx.response().end(res.result());
                } else {
                    ctx.fail(res.cause());
                }
            });
        }

        @Override
        public void start() throws Exception {
            // Template engine will be created just once and its instance will be passed to the render(...) static method.
            HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create();
            // Then we need to create a router instance.
            Router router = Router.router(vertx);
            // Thus we register a route for GET HTTP requests to the application root, i.e. "/", "/index.html"
            router.get().handler(ctx -> {
                // Render the template
                render(engine, ctx, "index");
            });
            // Thus we map a concrete application path with a given template for all POST HTTP requests matching this
            // address.
            router.post("/say-hi").handler(ctx -> {
                HttpServerRequest request = ctx.request();
                // Informing vert.x that we are awaiting form data
                request.setExpectMultipart(true).endHandler(req -> {
                    // Put the input from the form in the context, so that these values could be used for
                    // server side template rendering
                    ctx.put("first-name", request.getFormAttribute("first-name"));
                    ctx.put("family-name", request.getFormAttribute("family-name"));
                    // Render the template
                    render(engine, ctx, "say-hi");
                });
            });
            vertx.createHttpServer().requestHandler(router::accept).listen(HTTPConstants.SERVER_PORT);
        }
    }
}
