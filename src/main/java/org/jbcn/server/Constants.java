package org.jbcn.server;

public class Constants {

    public static final String USER_QUEUE = "USER:QUEUE";
    public static final String PAPER_QUEUE = "PAPER:QUEUE";
    public static final String AUTH_QUEUE = "AUTH:QUEUE";

    public static final int UNKNOW_ACTION_ERROR = 0;
    public static final int PERSISTENCE_ERROR = 1;
    public static final int VALIDATE_ERROR = 2;
    public static final int AUTH_ERROR = 3;

    public static final String UNKNOW_ACTION_ERROR_CODE ="error.event_bus.unknown_error";
    public static final String UNKNOW_USER_ERROR  = "error.user.unknown_user";
    public static final String UNKNOW_PAPER_ERROR  = "error.paper.unknown_paper";

    public static final String ERROR_USERNAME_MANDATORY = "error.auth.username_mandatory";
    public static final String ERROR_PASSWORD_MANDATORY = "error.auth.password_mandatory";
}
