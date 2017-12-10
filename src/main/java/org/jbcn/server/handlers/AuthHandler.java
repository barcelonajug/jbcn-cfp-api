package org.jbcn.server.handlers;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import org.jbcn.server.Constants;

public class AuthHandler {

    private static final Logger _log = LoggerFactory.getLogger(AuthHandler.class);

    private MongoAuth authProvider;
    private static JWTAuth jwtAuth;
    private long tokenExpirationTime = 60*60*3;

    public static JWTAuth getJwtAuth(Vertx vertx) {
        if(jwtAuth == null) {
            jwtAuth = JWTAuth.create(vertx, new JsonObject()
                    .put("keyStore", new JsonObject()
                            .put("type", "jceks")
                            .put("path", "keystore.jceks")
                            .put("password", "secret"))
            );
        }
        return jwtAuth;
    }

    public AuthHandler(Vertx vertx, MongoClient mongoClient) {
        JsonObject authProperties = new JsonObject();
        authProvider = MongoAuth.create(mongoClient, authProperties);
        authProvider.getHashStrategy().setSaltStyle(HashSaltStyle.NO_SALT);
    }

    public void onMessage(Message<JsonObject> message) {
        String action = message.headers().get("action");
        _log.debug("Received message");
        switch (action) {
            case "login":
                authenticate(message);
                break;
            default:
                message.fail(Constants.UNKNOW_ACTION_ERROR, Constants.UNKNOW_ACTION_ERROR_CODE);
                break;
        }
    }

    private void authenticate(Message<JsonObject> message) {
        String username = message.body().getString("username");
        String password = message.body().getString("password");
        JsonObject authInfo = new JsonObject();
        authInfo.put("username", username);

        String encodedPassword = password; //this.authProvider.getHashStrategy().computeHash(password, null);

        authInfo.put("password", encodedPassword);

        authProvider.authenticate(authInfo, res -> {
            if(res.succeeded()) {
                _log.info("Authenticate!");
                JsonObject result = new JsonObject();
                String token = jwtAuth.generateToken(new JsonObject(), new JWTOptions().setExpiresInSeconds(tokenExpirationTime));
                result.put("token", token);
                message.reply(result);
            } else {
                _log.info("Not Authenticate: "+res.cause().getMessage());
                message.fail(Constants.AUTH_ERROR, res.cause().getMessage());
            }
        });

    }
}
