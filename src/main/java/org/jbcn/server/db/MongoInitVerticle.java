package org.jbcn.server.db;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import org.jbcn.server.Constants;
import org.jbcn.server.handlers.AuthHandler;
import org.jbcn.server.handlers.PaperHandler;
import org.jbcn.server.handlers.UserHandler;

public class MongoInitVerticle extends AbstractVerticle {

    private MongoClient mongoClient;

    private UserHandler userDaoHandler;
    private PaperHandler paperDaoHandler;
    private AuthHandler authHandler;

    private static final Logger _log = LoggerFactory.getLogger(MongoInitVerticle.class);


    private Future<Void> prepareDatabase() {

        Future<Void> future = Future.future();
        JsonObject config = new JsonObject();
        config.put("db_name","jbcn");
        config.put("connection_string", "mongodb://localhost:27017");
        mongoClient = MongoClient.createNonShared(vertx, config);
        authHandler = new AuthHandler(vertx, mongoClient);
        paperDaoHandler = new PaperHandler(mongoClient);

        return future;

    }

    public void start(Future<Void> startFuture) throws Exception {

        this.prepareDatabase();
        vertx.eventBus().consumer(Constants.PAPER_QUEUE, this.paperDaoHandler::onMessage);
        vertx.eventBus().consumer(Constants.AUTH_QUEUE, this.authHandler::onMessage);
    }



}
