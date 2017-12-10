package org.jbcn.server.handlers;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.BsonElement;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jbcn.server.Constants;

import javax.xml.parsers.DocumentBuilder;
import java.util.Date;

public class UserHandler extends AbstractVerticle {

    private static final Logger _log = LoggerFactory.getLogger(UserHandler.class);


    public static final String FIELD_PASSWORD = "password";
    public static final String FIELD_USERNAME = "username";


    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    //private MongoCollection<Document> collection;

    private static final String collection_name = "user";

    private Future<Void> prepareDatabase() {

        this.mongoClient = MongoClients.create("mongodb://localhost:27017");
        this.mongoDatabase = mongoClient.getDatabase("jbcn");
        //this.collection = mongoDatabase.getCollection(collection_name);


        Future<Void> future = Future.future();
        JsonObject config = new JsonObject();
        config.put("db_name","jbcn");
        config.put("connection_string", "mongodb://localhost:27017");

        return future;

    }


    public void start(Future<Void> startFuture) throws Exception {
        this.prepareDatabase();
        vertx.eventBus().consumer(Constants.USER_QUEUE, this::onMessage);
    }

    public void onMessage(Message<JsonObject> message) {
        String action = message.headers().get("action");
        switch (action) {
            case "user-list":
                listUsers(message);
                break;
            case "user-search":
                searchUsers(message);
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
                _log.info("Default message");
                message.fail(Constants.UNKNOW_ACTION_ERROR, Constants.UNKNOW_ACTION_ERROR_CODE);
                break;
        }
    }

    private void listUsers(Message<JsonObject> message) {

        JsonObject data = new JsonObject();
        JsonArray users = new JsonArray();

        this.mongoDatabase.getCollection(collection_name).find().forEach((document) -> {
            users.add(new JsonObject(document.toJson()));
        }, (result, error) -> {
            data.put("items", users);
            message.reply(data);
        });
    }

    private void searchUsers(Message<JsonObject> message) {


        String term = message.body().getString("term");
        int size = message.body().getInteger("size");
        int page = message.body().getInteger("page");
        String sort_column = message.body().getString("sort");
        boolean asc = message.body().getBoolean("asc");
        int skip = page*size;



        if(term==null) term = "";
        JsonArray items = new JsonArray();
        JsonObject data = new JsonObject();

        if(sort_column == null) sort_column = "username";

        final Bson query = Filters.regex("username", term);
        final Bson sort = asc?Sorts.ascending(sort_column): Sorts.descending(sort_column);

        this.mongoDatabase.getCollection(collection_name).count(query, (count, error) -> {
            if(error!=null) {
                message.fail(Constants.PERSISTENCE_ERROR, error.getMessage());
            } else {
                data.put("total", count);

                this.mongoDatabase.getCollection(collection_name)
                        .find(query)
                        .sort(sort)
                        .skip(skip)
                        .limit(size)
                        .forEach((doc) -> {
                    _log.info("USER SEARCH ITEM REPLY");
                    _log.info("doc:"+doc.toJson());
                    items.add(new JsonObject(doc.toJson()));
                }, (result, error2) -> {
                    _log.info("USER SERACH FINISH");
                    if(error!=null) {
                        _log.error("Error in search", error2);
                        message.fail(Constants.PERSISTENCE_ERROR, error2.getMessage());
                    } else {
                        data.put("items", items);
                        message.reply(data);
                    }
                });
            }
        });
    }

    private void getUser(Message<JsonObject> message) {

        String id = message.body().getString("id");
        _log.info("GET USER:"+id);

        try {
            ObjectId oid = new ObjectId(id);
            this.mongoDatabase.getCollection(collection_name).find(Filters.eq("_id", oid)).first((user, error) -> {
                _log.info("GET USER FIND RESULT");
                if(error != null) {
                    message.fail(Constants.PERSISTENCE_ERROR, error.getMessage());
                } else {
                    if(user == null) {
                        message.fail(Constants.PERSISTENCE_ERROR, Constants.UNKNOW_USER_ERROR);
                    } else {
                        JsonObject instance = new JsonObject(user.toJson());
                        instance.remove("password");
                        message.reply(instance);
                    }

                }
            });
        } catch(Exception e) {
            _log.error("Error in getUser", e);
            message.fail(Constants.PERSISTENCE_ERROR, Constants.UNKNOW_USER_ERROR);
        }

    }

    private void addUser(Message<JsonObject> message) {
        JsonObject user = message.body();

        Document newUser = new Document()
                .append("username", user.getString("username"))
                .append("password", user.getString("password"))
                .append("email", user.getString("email"))
                .append("createdDate", new Date())
                .append("lastUpdate", new Date());

        this.mongoDatabase.getCollection(collection_name).insertOne(newUser, (Void result, Throwable error) -> {
            if(error!=null) {
                message.fail(Constants.PERSISTENCE_ERROR, error.getMessage());
            } else {
                message.reply(new JsonObject());
            }
        });
    }

    private void updateUser(Message<JsonObject> message) {

        JsonObject user = message.body();

        user.put("lastUpdate", new Date().getTime());

        ObjectId oid = new ObjectId(user.getString("_id"));

        this.mongoDatabase.getCollection(collection_name).find(Filters.eq("_id", oid))
                .first((result, error) -> {
                    if(error!=null) {
                        message.fail(Constants.PERSISTENCE_ERROR, error.getMessage());
                    } else {
                        if(result == null) {
                            message.fail(Constants.PERSISTENCE_ERROR, Constants.UNKNOW_USER_ERROR);
                        } else {
                            _log.info("There is user!:"+result.toJson());

                            this.mongoDatabase.getCollection(collection_name).updateOne(Filters.eq("_id", oid), Updates.combine(Updates.set("username", user.getString("username")),Updates.set("email", user.getString("email")), Updates.set("lastUpdate", new Date())), (updateResult, updateError) -> {
                                if(updateError != null) {
                                    _log.error("Error in update:", updateError.getCause());
                                    message.fail(Constants.PERSISTENCE_ERROR, updateError.getMessage());
                                } else {
                                    _log.info("Updated count:"+updateResult.getModifiedCount());
                                    if(updateResult.getModifiedCount() == 0) {
                                        message.fail(Constants.PERSISTENCE_ERROR, Constants.UNKNOW_USER_ERROR);
                                    } else {
                                        message.reply(new JsonObject());
                                    }
                                }
                            });
                        }
                    }
                });

    }

    private void deleteUser(Message<JsonObject> message) {
        String id = message.body().getString("id");
        ObjectId oid = new ObjectId(id);
        this.mongoDatabase.getCollection(collection_name).deleteOne(Filters.eq("_id", oid), (result, error) -> {
            if(error != null) {
                _log.error("Error deleting user",error);
                message.fail(Constants.PERSISTENCE_ERROR, error.getMessage());
            } else {
                message.reply(new JsonObject());
            }
        });
    }

}
