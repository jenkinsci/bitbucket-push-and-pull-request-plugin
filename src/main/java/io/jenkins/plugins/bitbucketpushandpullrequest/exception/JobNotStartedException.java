package io.jenkins.plugins.bitbucketpushandpullrequest.exception;

import java.io.Serial;

public class JobNotStartedException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public JobNotStartedException(String msg) {
        super(msg);
    }
}
