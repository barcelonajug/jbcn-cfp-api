package org.jbcn.server.controllers;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import org.jbcn.server.Constants;
import org.jbcn.server.utils.ResponseUtils;

public class AuthController {

    private static final Logger _log = LoggerFactory.getLogger(AuthController.class);

    private Vertx vertx;

    public AuthController(Vertx vertx) {
        this.vertx = vertx;
    }

    public void login(RoutingContext context) {
        _log.debug("Login");
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "login");
        JsonObject payload = context.getBodyAsJson();
        String username = payload.getString("username");
        String password = payload.getString("password");

        if(username==null || username.isEmpty()) {
            ResponseUtils.errorResult(context, Constants.ERROR_USERNAME_MANDATORY);
            return;
        }
        if(password == null || password.isEmpty()) {
            ResponseUtils.errorResult(context, Constants.ERROR_PASSWORD_MANDATORY);
            return;
        }
        JsonObject params = new JsonObject()
                .put("username", username)
                .put("password", password);
        vertx.eventBus().send(Constants.AUTH_QUEUE, params, options, reply -> {
            if(reply.succeeded()) {
                JsonObject body = (JsonObject) reply.result().body();
                String token = body.getString("token");
                _log.info("Login token:"+token);
                ResponseUtils.loginResult(context, token);
            } else {
                ResponseUtils.errorResult(context, reply.cause().getMessage());
            }
        });
    }

    public void logout(RoutingContext context) {
        _log.info("logout!");
        context.clearUser();
        ResponseUtils.simpleResult(context);
    }

}
