//--------------------------------------------------
// Class UserSubjectiveException
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.exception;

public class UserViolationException extends BusinessLogicException {
    public UserViolationException() {
        super();
    }

    public UserViolationException(String message) {
        super(message);
    }

    public UserViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserViolationException(Throwable cause) {
        super(cause);
    }

    public UserViolationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
