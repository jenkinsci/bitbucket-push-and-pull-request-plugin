package io.jenkins.plugins.bitbucketpushandpullrequest.common;

import io.jenkins.plugins.bitbucketpushandpullrequest.exception.BitBucketPPRRepositoryNotParsedException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class BitBucketPPRUtilsTest {

    @ParameterizedTest
    @MethodSource("gitSSHRepos")
    void testExtractRepositoryNameFromSSHUri(String repository, String expectedResultWorkspace, String expectedResultRepository) {
        BitBucketPPRUtils utils = new BitBucketPPRUtils();
        Map<String, String> res = utils.extractRepositoryNameFromSSHUri(repository);

        assertEquals(expectedResultWorkspace, res.get(BitBucketPPRUtils.BB_WORKSPACE));
        assertEquals(expectedResultRepository, res.get(BitBucketPPRUtils.BB_REPOSITORY));
    }

    private static Stream<Arguments> gitSSHRepos() {
        return Stream.of(
                arguments("git@bitbucket.org:work-space/reponame.git","work-space", "reponame"),
                arguments("git@bitbucket.org:workspace/reponame.git", "workspace", "reponame"),
                arguments("git@bitbucket.org:work&&space/reponame.git", "work&&space", "reponame"));
    }

    @ParameterizedTest
    @MethodSource("gitHTTPSRepos")
    void testExtractRepositoryNameFromHTTPSUrl(String repository, String expectedResultWorkspace, String expectedResultRepository) throws BitBucketPPRRepositoryNotParsedException {
        BitBucketPPRUtils utils = new BitBucketPPRUtils();
        Map<String, String> res = utils.extractRepositoryNameFromHTTPSUrlForTest(repository);

        assertEquals(expectedResultWorkspace, res.get(BitBucketPPRUtils.BB_WORKSPACE));
        assertEquals(expectedResultRepository, res.get(BitBucketPPRUtils.BB_REPOSITORY));
    }

    private static Stream<Arguments> gitHTTPSRepos() {
        return Stream.of(
                arguments("https://username@bitbucket.org/work-space/reponame","work-space", "reponame"),
                arguments("https://username@bitbucket.org/workspace/reponame", "workspace", "reponame"),
                arguments("https://username@bitbucket.org/work&&space/reponame/", "work&&space", "reponame"));
    }
}