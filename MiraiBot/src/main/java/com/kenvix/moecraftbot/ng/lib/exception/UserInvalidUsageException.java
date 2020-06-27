//--------------------------------------------------
// Class UserSubjectiveException
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.exception;

public class UserInvalidUsageException extends UserViolationException {
    public UserInvalidUsageException() {
        super();
    }

    public UserInvalidUsageException(String message) {
        super(message);
    }

    public UserInvalidUsageException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserInvalidUsageException(Throwable cause) {
        super(cause);
    }

    public UserInvalidUsageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
