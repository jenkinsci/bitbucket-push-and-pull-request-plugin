package io.jenkins.plugins.bitbucketpushandpullrequest.exception;

public class TriggerNotSetException extends Exception {

  public TriggerNotSetException() {
    super();
  }

  public TriggerNotSetException(String message) {
    super(message);
  }

}
