//--------------------------------------------------
// Class InvalidAuthorizationException
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.exception;

public class InvalidAuthorizationException extends UserViolationException {
    public InvalidAuthorizationException() {
    }

    public InvalidAuthorizationException(String message) {
        super(message);
    }

    public InvalidAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAuthorizationException(Throwable cause) {
        super(cause);
    }

    public InvalidAuthorizationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
