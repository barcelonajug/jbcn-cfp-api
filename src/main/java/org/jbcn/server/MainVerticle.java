package org.jbcn.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class MainVerticle extends AbstractVerticle {

    private MongoClient mongoClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Future<String> httpVerticleDeployment = Future.future();
        vertx.deployVerticle("org.jbcn.server.http.HttpServerVerticle", httpVerticleDeployment.completer());
        vertx.deployVerticle("org.jbcn.server.db.MongoInitVerticle", httpVerticleDeployment.completer());
        vertx.deployVerticle("org.jbcn.server.handlers.UserHandler", httpVerticleDeployment.completer());
        httpVerticleDeployment.setHandler(ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

}
