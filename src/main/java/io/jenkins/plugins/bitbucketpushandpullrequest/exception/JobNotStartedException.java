package io.jenkins.plugins.bitbucketpushandpullrequest.exception;

public class JobNotStartedException extends Exception {

    public JobNotStartedException(String msg) {
        super(msg);
    }

    private static final long serialVersionUID = 1L;
}