package org.jbcn.server.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import org.jbcn.server.Constants;
import org.jbcn.server.controllers.AuthController;
import org.jbcn.server.controllers.PaperController;
import org.jbcn.server.controllers.UserController;
import org.jbcn.server.utils.ResponseUtils;
import io.vertx.ext.web.handler.CorsHandler;
public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger _log = LoggerFactory.getLogger(HttpServerVerticle.class);

    private UserController userController = null;
    private PaperController paperController = null;
    private AuthController authController = null;


    public void start(Future<Void> startFuture) throws Exception {

        userController = new UserController(vertx);
        paperController = new PaperController(vertx);
        authController = new AuthController(vertx);

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route().handler(CorsHandler.create("http://localhost:4200")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
                .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowCredentials(true)
                .allowedHeader("Authorization")
                .allowedHeader("Access-Control-Allow-Method")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Content-Type"));

        router.get("/").handler(this::indexHandler);

        JWTAuth jwt = org.jbcn.server.handlers.AuthHandler.getJwtAuth(vertx);

        router.route("/api/*").handler(JWTAuthHandler.create(jwt, null));

        router.get("/echo/:message").handler(this::indexEcho);

        //User routes
        router.get("/api/user").handler(this.userController::getUsers);
        router.route("/api/user/*").handler(BodyHandler.create());
        router.post("/api/user/search").handler(this.userController::searchUser);
        router.get("/api/user/:id").handler(this.userController::getUser);
        router.put("/api/user/:id").handler(this.userController::updateUser);
        router.post("/api/user").handler(this.userController::addUser);
        router.delete("/api/user/:id").handler(this.userController::deleteUser);

        //Paper routes
        router.get("/api/paper").handler(this.paperController::getPapers);
        router.route("/api/paper/*").handler(BodyHandler.create());
        router.get("/api/paper/:id").handler(this.paperController::getPaper);
        router.put("/api/paper").handler(this.paperController::updatePaper);
        router.post("/api/paper").handler(this.paperController::addPaper);
        router.post("/api/paper/:id").handler(this.paperController::deletePaper);

        //Auth routes
        router.route("/login/*").handler(BodyHandler.create());
        router.post("/login").handler(this.authController::login);
        router.route("/logout/*").handler(BodyHandler.create());
        router.get("/logout").handler(this.authController::logout);



        server.requestHandler(router::accept).listen(8080, ar -> {
            if (ar.succeeded()) {
                _log.debug("HTTP server running on port 8080");
                startFuture.complete();
            } else {
                _log.error("Could not start a HTTP server", ar.cause());
                startFuture.fail(ar.cause());
            }
        });
    }

    private void indexHandler(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.putHeader("content-type","application/json");
        JsonObject data = new JsonObject();
        response.end(data.toBuffer());
    }

    private void indexEcho(RoutingContext context) {
        String message = context.request().getParam("message");

        HttpServerResponse response = context.response();
        response.putHeader("content-type","application/json");
        JsonObject data = new JsonObject();
        data.put("echo",message+":"+System.currentTimeMillis());
        response.end(data.toBuffer());
    }

}
