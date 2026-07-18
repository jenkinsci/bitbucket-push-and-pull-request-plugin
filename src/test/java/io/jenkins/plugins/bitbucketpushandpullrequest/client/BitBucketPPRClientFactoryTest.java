/*******************************************************************************
 * The MIT License
 *
 * Copyright (C) 2018-2026, Christian Del Monte.
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

package io.jenkins.plugins.bitbucketpushandpullrequest.client;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.bitbucketpushandpullrequest.event.BitBucketPPREventContext;
import org.junit.jupiter.api.Test;

/**
 * Freezes the legacy compatibility contract of the deprecated client layer: the factory keeps
 * returning the legacy concrete types with their visitors wired, and a custom visitor installed
 * through accept() keeps being invoked by send(). External code compiled against 4.0.0 relies on
 * exactly this behavior; a refactoring must not silently turn it into a no-op again.
 */
@SuppressWarnings("deprecation")
class BitBucketPPRClientFactoryTest {

  @Test
  void createClientKeepsReturningTheLegacyConcreteTypes() throws Exception {
    BitBucketPPREventContext context = mock(BitBucketPPREventContext.class);

    assertInstanceOf(BitBucketPPRCloudClient.class,
        BitBucketPPRClientFactory.createClient(BitBucketPPRClientType.CLOUD, context));
    assertInstanceOf(BitBucketPPRServerClient.class,
        BitBucketPPRClientFactory.createClient(BitBucketPPRClientType.SERVER, context));
  }

  @Test
  void legacyClientSendInvokesTheAcceptedVisitor() throws Exception {
    BitBucketPPREventContext context = mock(BitBucketPPREventContext.class);
    StandardCredentials credentials = mock(StandardCredentials.class);
    when(context.getStandardCredentials()).thenReturn(credentials);
    BitBucketPPRClientVisitor visitor = mock(BitBucketPPRClientVisitor.class);

    BitBucketPPRClient client = new BitBucketPPRServerClient(context);
    client.accept(visitor);
    client.send("https://bitbucket.example.test/rest/build-status", "{}");

    verify(visitor).send(credentials, "https://bitbucket.example.test/rest/build-status", "{}");
  }
}
