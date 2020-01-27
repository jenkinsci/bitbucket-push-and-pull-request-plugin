package io.jenkins.plugins.bitbucketpushandpullrequest.exception;

public class JobNotStartedException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public JobNotStartedException(String msg) {
        super(msg);
    }
}