package io.jenkins.plugins.bitbucketpushandpullrequest.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BitBucketPPRConfigAttributesTest {

  private final Class<BitBucketPPRPluginConfig> config = BitBucketPPRPluginConfig.class;

  @Test
  void settersHaveGetters() {
    for (Method method : config.getMethods()) {
      final String methodName = method.getName();
      if (method.getParameterCount() != 1 || !methodName.startsWith("set")) {
        // Not an accessor, ignore
        continue;
      }

      final String s = methodName.substring(3);
      assertTrue(hasGetter(s), s);
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
