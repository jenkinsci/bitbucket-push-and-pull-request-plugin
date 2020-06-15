/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2020, CloudBees, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package io.jenkins.plugins.bitbucketpushandpullrequest.observer;

import java.io.PrintStream;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import hudson.model.Result;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.BitBucketPPRClientFactory;
import io.jenkins.plugins.bitbucketpushandpullrequest.client.BitBucketPPRClientType;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;

public class BitBucketPPRPushCloudObserver extends BitBucketPPRHandlerTemplate
    implements BitBucketPPRObserver {
  static final Logger LOGGER = Logger.getLogger(BitBucketPPRPushCloudObserver.class.getName());

  private BitBucketPPREventContext context;

  @Override
  public void getNotification(BitBucketPPREvent event) {
    context = event.getContext();
    event.setEventHandler(this);
    event.runHandler();
  }

  @Override
  public void setBuildStatusOnFinished() {
    LOGGER.info("Set build status on finished for push called.");
    try {
      BitBucketPPRAction bitbucketAction = context.getAction();
      List<String> commitLinks = bitbucketAction.getCommitLinks();
      int buildNumber = context.getRun().getNumber();
      Result result = context.getRun().getResult();
      String absoluteUrl = context.getAbsoluteUrl();

      for (String link : commitLinks) {
        String url = link + "/statuses/build";
        String payload = "{\"key\": \"" + buildNumber + "\", \"url\": \"" + absoluteUrl + "\", ";
        payload += result == Result.SUCCESS ? "\"state\": \"SUCCESSFUL\""
            : result == Result.ABORTED ? "\"state\": \"STOPPED\"" : "\"state\": \"FAILED\"";
        payload += " }";

        callClient(url, payload);
      }
    } catch (Throwable e) {
      LOGGER.info("Set build status on finished for push called but something went wrong.");
      LOGGER.info(e.getMessage());
    }
  }

  @Override
  public void setBuildStatusInProgress() {
    LOGGER.info("Set build status in progress for push called.");

    try {
      BitBucketPPRAction bitbucketAction = context.getAction();
      List<String> commitLinks = bitbucketAction.getCommitLinks();
      int buildNumber = context.getRun().getNumber();
      String absoluteUrl = context.getAbsoluteUrl();

      for (String link : commitLinks) {
        String url = link + "/statuses/build";
        String payload = "{\"key\": \"" + buildNumber + "\", \"url\": \"" + absoluteUrl
            + "\", \"state\": \"INPROGRESS\" }";

        callClient(url, payload);
      }
    } catch (Throwable e) {
      System.setErr(createLoggingProxy(System.err));
      LOGGER.info("Set build status in progress for push called but something went wrong.");
      e.printStackTrace();
    }
  }

  public void callClient(@Nonnull String url, @Nonnull String payload) throws Throwable {
    BitBucketPPRClientFactory.createClient(BitBucketPPRClientType.CLOUD,
        getUserRemoteConfigs(context.getScmTrigger()), context.getRun())
        .sendWithUsernamePasswordCredentials(url, payload);
  }

  public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
    return new PrintStream(realPrintStream) {
      public void print(final String string) {
        LOGGER.info(string);
      }

      public void println(final String string) {
        LOGGER.info(string);
      }
    };
  }

}
