package io.jenkins.plugins.bitbucketpushandpullrequest.observer;

import hudson.model.Result;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREvent;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class BitBucketPPRMsTeamsObserver extends BitBucketPPRHandlerTemplate
    implements BitBucketPPRObserver {
  private String msTeamsNotificationUrl;
  private Map<String, Object> map;

  public BitBucketPPRMsTeamsObserver(String msTeamsNotificationUrl) {
    super();
    this.msTeamsNotificationUrl = msTeamsNotificationUrl;
    this.map = new HashMap<>();
    fillDefaultMap();
  }

  private void fillDefaultMap() {
    map.put("type", "message");
    map.put("attachments", new Object[] {new HashMap<String, Object>()});
    HashMap<String, Object> attachment =
        (HashMap<String, Object>) ((Object[]) map.get("attachments"))[0];
    attachment.put("contentType", "application/vnd.microsoft.card.adaptive");
    attachment.put("content", new HashMap<String, Object>());
    HashMap<String, Object> content = (HashMap<String, Object>) attachment.get("content");
    content.put("$schema", "http://adaptivecards.io/schemas/adaptive-card.json");
    content.put("type", "AdaptiveCard");
    content.put("version", "1.2");
    content.put("body", new Object[] {new HashMap<String, Object>()});
    HashMap<String, Object> body = (HashMap<String, Object>) ((Object[]) content.get("body"))[0];
    body.put("type", "TextBlock");
  }

  @Override
  public void getNotification(BitBucketPPREvent event) {
    context = event.getContext();
    event.setEventHandler(this);
    event.runHandler();
  }

  @Override
  public void setBuildStatusOnFinished() throws MalformedURLException {
    Result result = context.getRun().getResult();
    String state =
        result == Result.SUCCESS ? "SUCCESSFUL" : result == Result.ABORTED ? "STOPPED" : "FAILED";

    String message = "Build " + state + " for " + context.getRun().getFullDisplayName();
    setBody(message);
    callClient(msTeamsNotificationUrl, map);
  }

  private void setBody(String message) {
    String text = "Submitted response:" + message;
    HashMap<String, Object> attachment =
        (HashMap<String, Object>) ((Object[]) map.get("attachments"))[0];
    HashMap<String, Object> content = (HashMap<String, Object>) attachment.get("content");
    HashMap<String, Object> body = (HashMap<String, Object>) ((Object[]) content.get("body"))[0];
    body.put("text", text);
  }

  @Override
  public void setBuildStatusInProgress() throws MalformedURLException {}
}
