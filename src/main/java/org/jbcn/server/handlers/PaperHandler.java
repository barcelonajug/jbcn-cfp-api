package org.jbcn.server.handlers;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import org.jbcn.server.Constants;

import java.util.Date;

public class PaperHandler {

    private static final Logger _log = LoggerFactory.getLogger(PaperHandler.class);

    private static final String FIELD_ID  = "_id";
    private static final String FIELD_FULL_NAME = "fullName";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_BIOGRAPHY = "biography";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_LEVEL = "level";
    private static final String FIELD_ABSTRACT = "abstract";
    private static final String FIELD_COMMENTS = "comments";
    private static final String FIELD_TWITTER = "twitter";
    private static final String FIELD_WEB = "web";
    private static final String FIELD_LINKEDIN = "linkedIn";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_TRAVEL_COSTS = "travelCosts";
    private static final String FIELD_STATE = "state";
    private MongoClient mongoClient;

    public PaperHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void onMessage(Message<JsonObject> message) {
        String action = message.headers().get("action");
        _log.info("PaperHandler Received message:"+action);
        switch (action) {
            case "paper-list":
                listPapers(message);
                break;
            case "paper-get":
                getPaper(message);
                break;
            case "paper-search":
                searchPaper(message);
                break;
            case "paper-add":
                addPaper(message);
                break;
            case "paper-update":
                updatePaper(message);
                break;
            case "paper-delete":
                deletePaper(message);
                break;
            default:
                _log.warn("Unknow action:"+action);
                message.fail(Constants.UNKNOW_ACTION_ERROR, Constants.UNKNOW_ACTION_ERROR_CODE);
                break;
        }
    }

    private void listPapers(Message<JsonObject> message) {
        this.mongoClient.find("Paper", new JsonObject(), res -> {
            if(res.succeeded()) {
                JsonObject result = new JsonObject();
                result.put("list", res.result());
                message.reply(result);
            } else {
                message.fail(Constants.PERSISTENCE_ERROR, res.cause().getMessage());
            }

        });
    }

    private void searchPaper(Message<JsonObject> message) {

    }

    private void getPaper(Message<JsonObject> message) {

        String id = message.body().getString("id");
        _log.debug("Getting paper:"+id);
        JsonObject query = new JsonObject();
        query.put("_id", id);

        this.mongoClient.find("Paper", query, res -> {
            if(res.succeeded()) {
                JsonObject result = new JsonObject();
                if(res.result().size()>0) {
                    result.put("instance", res.result().get(0));
                }
                message.reply(result);
            } else {
                message.fail(Constants.PERSISTENCE_ERROR, res.cause().getMessage());
            }
        });
    }

    private String validatePaper(JsonObject paper) {
        return null;
    }

    private void addPaper(Message<JsonObject> message) {
        _log.info("Adding Papger");
        JsonObject paper = message.body();
        _log.info("Paper:"+paper.toString());
        /*String error = validatePaper(paper);
        if(error!=null) {
            message.fail(Constants.VALIDATE_ERROR, error);
        }*/

        paper.put("createdDate", new Date().getTime());
        paper.put("lastUpdate", new Date().getTime());
        this.mongoClient.save("Paper", paper, res -> {
            if(res.succeeded()) {
                _log.info("Paper saved");
                message.reply(new JsonObject());
            } else {
                _log.error("Paper save error", res.cause());
                message.fail(Constants.PERSISTENCE_ERROR, res.cause().getMessage());
            }
        });
    }

    private void updatePaper(Message<JsonObject> message) {
        JsonObject paper = message.body();
        paper.put("lastUpdate", new Date().getTime());
        this.mongoClient.save("Paper", paper, res -> {
            if(res.succeeded()) {
                message.reply(new JsonObject());
            } else {
                message.fail(Constants.PERSISTENCE_ERROR, res.cause().getMessage());
            }
        });
    }

    private void deletePaper(Message<JsonObject> message) {
        String id = message.body().getString("id");
        if(id!=null) {
            JsonObject query = new JsonObject();
            query.put("_id",id);
            this.mongoClient.removeDocument("Paper", query, res ->{
                if(res.succeeded()) {
                    message.reply(new JsonObject());
                } else {
                    message.fail(Constants.PERSISTENCE_ERROR, res.cause().getMessage());
                }
            });
        }
    }

}
