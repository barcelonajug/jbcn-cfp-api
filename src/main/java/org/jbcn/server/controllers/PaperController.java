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

public class PaperController {

    private static final Logger _log = LoggerFactory.getLogger(PaperController.class);

    private Vertx vertx;

    public PaperController(Vertx vertx) {
        this.vertx = vertx;
    }

    public void getPaper(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.putHeader("content-type","application/json");
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "paper-get");

        String id = context.request().getParam("id");
        _log.debug("Get paper with id:" + id);

        JsonObject params = new JsonObject();
        params.put("id",id);

        if(id == null) {
            JsonObject body = new JsonObject();
            body.put("status", false);
            body.put("error", Constants.UNKNOW_PAPER_ERROR);
            response.end(body.toBuffer());
        } else {
            vertx.eventBus().send(Constants.PAPER_QUEUE, params, options, reply -> {
                JsonObject body = new JsonObject();
                if(reply.succeeded()) {
                    JsonObject paper = (JsonObject) reply.result().body();
                    JsonObject data = new JsonObject();
                    data.put("instance", paper);
                    body.put("data", data);
                    body.put("status", true);
                    _log.debug("body:"+body.toString());
                    response.end(body.toBuffer());
                } else {
                    String error = reply.cause().getMessage();
                    ResponseUtils.errorResult(context, error);
                }

            });

        }
    }


    public void getPapers(final RoutingContext context) {
        _log.debug("Get Papers");
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "paper-list");
        vertx.eventBus().send(Constants.PAPER_QUEUE, new JsonObject(), options, reply -> {
            ResponseUtils.listResult(context, reply);
        });

    }


    public void updatePaper(final RoutingContext context) {
        JsonObject paperJson = new JsonObject(context.getBodyAsString());
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "paper-update");
        String id = context.request().getParam("id");
        paperJson.put("_id", id);
        this.savePaper(context, paperJson, options);
    }

    public void addPaper(final RoutingContext context) {
        _log.debug("Adding paper:"+context.getBodyAsString());
        JsonObject paperJson = new JsonObject(context.getBodyAsString());
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "paper-add");
        this.savePaper(context, paperJson, options);
    }

    private void savePaper(RoutingContext context, JsonObject paperJson, DeliveryOptions options) {
        _log.info("Saving paper");
        vertx.eventBus().send(Constants.PAPER_QUEUE, paperJson, options, reply -> {
            if(reply.succeeded()) {
                _log.info("Returning result");
                ResponseUtils.simpleResult(context);
            } else {
                _log.error("Save paper Error", reply.cause());
                ResponseUtils.errorResult(context, reply.cause().getMessage());
            }
        });
    }

    public void deletePaper(RoutingContext context) {
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "paper-delete");
        String id = context.request().getParam("id");
        if(id == null) {
            ResponseUtils.errorResult(context, Constants.UNKNOW_PAPER_ERROR);
        } else {
            JsonObject params = new JsonObject();
            params.put("id",id);
            vertx.eventBus().send(Constants.PAPER_QUEUE, params, options, reply -> {
                ResponseUtils.simpleResult(context);
            });
        }
    }

}
