package org.jbcn.server.controllers;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import org.jbcn.server.Constants;
import org.jbcn.server.utils.ResponseUtils;

public class UserController {

    private static final Logger _log = LoggerFactory.getLogger(UserController.class);

    private Vertx vertx;

    public UserController(Vertx vertx) {
        this.vertx = vertx;
    }

    public void getUser(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.putHeader("content-type","application/json");
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "user-get");
        JsonObject params = new JsonObject();
        String id = context.request().getParam("id");
        _log.debug("Get user with id:"+id);
        params.put("id",id);
        if(id == null) {
            JsonObject body = new JsonObject();
            body.put("status", false);
            body.put("error", Constants.UNKNOW_USER_ERROR);
            response.end(body.toBuffer());
        } else {
            vertx.eventBus().send(Constants.USER_QUEUE, params, options, reply -> {
                JsonObject body = new JsonObject();
                if(reply.succeeded()) {
                    JsonObject user = (JsonObject) reply.result().body();
                    JsonObject data = new JsonObject();
                    data.put("instance", user);
                    body.put("data", data);
                    _log.debug("body:"+body.toString());
                    body.put("status", true);
                } else {
                    String error = reply.cause().getMessage();
                    body.put("status", false);
                    body.put("error", error);
                }
                response.end(body.toBuffer());
            });
        }


    }

    public void getUsers(final RoutingContext context) {
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "user-list");
        vertx.eventBus().send(Constants.USER_QUEUE, new JsonObject(), options, reply -> {
            ResponseUtils.listResult(context, reply);
        });

    }

    public void updateUser(final RoutingContext context) {

        JsonObject userJson = new JsonObject(context.getBodyAsString());
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "user-update");
        String id = context.request().getParam("id");
        userJson.put("_id", id);
        this.saveUser(context, userJson, options);

    }

    public void addUser(RoutingContext context) {
        _log.debug("Adding user:"+context.getBodyAsString());
        JsonObject userJson = new JsonObject(context.getBodyAsString());
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "user-add");
        this.saveUser(context, userJson, options);
    }

    public void saveUser(RoutingContext context, JsonObject userJson, DeliveryOptions options) {
        vertx.eventBus().send(Constants.USER_QUEUE, userJson, options, reply -> {
            ResponseUtils.simpleResult(context);
        });
    }

    public void deleteUser(RoutingContext context) {
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "user-delete");
        String id = context.request().getParam("id");
        if(id == null) {
            ResponseUtils.errorResult(context, Constants.UNKNOW_USER_ERROR);
        } else {
            JsonObject params = new JsonObject();
            params.put("id",id);
            vertx.eventBus().send(Constants.USER_QUEUE, params, options, reply -> {
                ResponseUtils.simpleResult(context);
            });
        }
    }



}
