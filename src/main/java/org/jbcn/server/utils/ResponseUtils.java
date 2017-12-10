package org.jbcn.server.utils;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ResponseUtils {

    public static void listResult(RoutingContext context, AsyncResult<Message<Object>> reply) {
        JsonObject body = new JsonObject();
        if(reply.succeeded()) {
            JsonObject result = (JsonObject) reply.result().body();
            body.put("data", result);
            body.put("status", true);
        } else {
            String error = reply.cause().getMessage();
            body.put("status", false);
            body.put("error", error);
        }
        sendBody(context,body);
    }

    public static void errorResult(RoutingContext context, String errorMessage) {
        JsonObject body = new JsonObject();
        body.put("status", false);
        body.put("error", errorMessage);
        sendBody(context,body);
    }

    public static void simpleResult(RoutingContext context) {
        JsonObject body = new JsonObject();
        body.put("status", true);
        sendBody(context,body);
    }

    public static void loginResult(RoutingContext context, String token) {
        JsonObject body = new JsonObject();
        body.put("status", true);
        body.put("data", new JsonObject().put("token", token));
        sendBody(context, body);
    }

    private static void sendBody(RoutingContext context, JsonObject body) {
        HttpServerResponse response = context.response();
        response.putHeader("content-type","application/json");
        response.end(body.toBuffer());

    }

    public static void searchResult(RoutingContext context, AsyncResult<Message<Object>> reply) {
        JsonObject body = new JsonObject();
        JsonObject data = (JsonObject) reply.result().body();
        body.put("status", true);
        body.put("data", data);
        sendBody(context, body);
    }
}
