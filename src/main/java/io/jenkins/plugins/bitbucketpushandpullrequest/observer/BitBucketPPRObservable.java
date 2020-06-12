package io.jenkins.plugins.bitbucketpushandpullrequest.observer;

public interface BitBucketPPRObservable {
  public void addObserver(BitBucketPPRObserver observer);
  public void removeObserver(BitBucketPPRObserver observer);
  public void notifyObserver(BitBucketPPREventObject event);
}
