package org.jbcn.server.handlers;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import org.jbcn.server.Constants;

import java.util.Date;

public class UserHandler {

    private static final Logger _log = LoggerFactory.getLogger(UserHandler.class);


    public static final String FIELD_PASSWORD = "password";
    public static final String FIELD_USERNAME = "username";


    private MongoClient mongoClient;

    private static final String collection = "user";





    public UserHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void onMessage(Message<JsonObject> message) {
        String action = message.headers().get("action");
        _log.debug("Received message");
        switch (action) {
            case "user-list":
                listUsers(message);
                break;
            case "user-get":
                getUser(message);
                break;
            case "user-add":
                addUser(message);
                break;
            case "user-update":
                updateUser(message);
                break;
            case "user-delete":
                deleteUser(message);
                break;
            default:
                message.fail(Constants.UNKNOW_ACTION_ERROR, Constants.UNKNOW_ACTION_ERROR_CODE);
                break;
        }
    }

    private void listUsers(Message<JsonObject> message) {
        this.mongoClient.find(collection, new JsonObject(), res -> {
            if(res.succeeded()) {
                JsonObject result = new JsonObject();
                result.put("list", res.result());
                message.reply(result);
            } else {
                message.fail(Constants.PERSISTENCE_ERROR, res.cause().getMessage());
            }

        });
    }

    private void getUser(Message<JsonObject> message) {

        String id = message.body().getString("id");
        _log.debug("Getting user:"+id);
        JsonObject query = new JsonObject();
        query.put("_id", id);

        this.mongoClient.find(collection, query, res -> {
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

    private void addUser(Message<JsonObject> message) {
        JsonObject user = message.body();
        user.put("createdDate", new Date().getTime());
        user.put("lastUpdate", new Date().getTime());

        this.mongoClient.save(collection, user, res -> {
            if(res.succeeded()) {
                message.reply(new JsonObject());
            } else {
                message.fail(Constants.PERSISTENCE_ERROR, res.cause().getMessage());
            }
        });
    }

    private void updateUser(Message<JsonObject> message) {
        JsonObject user = message.body();
        user.put("lastUpdate", new Date().getTime());
        this.mongoClient.save(collection, user, res -> {
            if(res.succeeded()) {
                message.reply(new JsonObject());
            } else {
                message.fail(Constants.PERSISTENCE_ERROR, res.cause().getMessage());
            }
        });
    }

    private void deleteUser(Message<JsonObject> message) {
        String id = message.body().getString("id");
        if(id!=null) {
            JsonObject query = new JsonObject();
            query.put("_id",id);
            this.mongoClient.removeDocument(collection, query, res ->{
                if(res.succeeded()) {
                    message.reply(new JsonObject());
                } else {
                    message.fail(Constants.PERSISTENCE_ERROR, res.cause().getMessage());
                }
            });
        }
    }

}
