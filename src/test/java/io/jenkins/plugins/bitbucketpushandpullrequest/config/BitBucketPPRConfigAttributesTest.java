package io.jenkins.plugins.bitbucketpushandpullrequest.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class BitBucketPPRConfigAttributesTest {

  final private Class<BitBucketPPRPluginConfig> config = BitBucketPPRPluginConfig.class;

  @Test
  public void settersHaveGetters() {
    for (Method method : config.getMethods()) {
      final String methodName = method.getName();
      if (method.getParameterCount() != 1 || !methodName.startsWith("set")) {
        // Not an accessor, ignore
        continue;
      }

      final String s = methodName.substring(3);
      assertTrue(s, hasGetter(s));
    }
  }

  private boolean hasGetter(String s) {
    List<String> candidates = Arrays.asList("get" + s, "is" + s);
    for (Method m : config.getMethods()) {
      if (m.getParameterCount() == 0 && candidates.contains(m.getName())) {
        return true;
      }
    }
    return false;
  }
}
