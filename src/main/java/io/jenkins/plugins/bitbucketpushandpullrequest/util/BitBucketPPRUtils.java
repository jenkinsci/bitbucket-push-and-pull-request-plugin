/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2018, CloudBees, Inc.
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
package io.jenkins.plugins.bitbucketpushandpullrequest.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.logging.Logger;
import hudson.EnvVars;
import hudson.plugins.git.BranchSpec;

public class BitBucketPPRUtils {

  private static final Logger LOGGER = Logger.getLogger(BitBucketPPRUtils.class.getName());


  public static boolean matches(String allBranches, String branchName, EnvVars env) {
    String allowedBranches = allBranches != null ? allBranches : "";

    LOGGER.info("Following allowed branches patterns are set: " + allowedBranches);
    LOGGER.info("The branchName in action is: " + branchName);
    LOGGER.info("The environment variables are: " + env);

    BiFunction<List<String>, BiPredicate<String, EnvVars>, List<String>> filter =
        (List<String> list, BiPredicate<String, EnvVars> p) -> {
          List<String> results = new ArrayList<>();
          for (String t : list) {
            if (p.test(t, env)) {
              results.add(t);
            }
          }
          return results;
        };

    List<String> nonEmpty = filter.apply(Arrays.asList(allowedBranches.split(",")),
        (String branchSpec, EnvVars envVar) -> {
          BranchSpec pattern = new BranchSpec(branchSpec.trim());
          if (envVar == null) {
            return pattern.matches(branchName);
          } else {
            return pattern.matches(branchName, envVar);
          }
        });

    nonEmpty.forEach((String s) -> LOGGER.info("Matching branch: " + s));

    return nonEmpty.size() > 0;
  }
}
