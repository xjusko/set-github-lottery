package org.lottery;

import io.quarkiverse.githubapp.GitHubClientProvider;
import io.quarkiverse.githubapp.testing.GitHubAppTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHApp;
import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GHAuthenticatedAppInstallation;
import org.kohsuke.github.GHIssueQueryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.hamcrest.MatcherAssert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import static io.quarkiverse.githubapp.testing.GitHubAppMockito.mockPagedIterable;
import static io.quarkiverse.githubapp.testing.GitHubAppTesting.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@QuarkusTest
@GitHubAppTest
public class GitHubServiceTest {

    private final GitHubInstallationRef installationRef = new GitHubInstallationRef("set-github-lottery", 1234L);

    @Inject
    GitHubService gitHubService;

    @Test
    void listRepositories() throws IOException {
        var repoRef = new GitHubRepositoryRef(installationRef, "xjusko/lottery-bot-test");

        var queryIssuesBuilderMock = Mockito.mock(GHIssueQueryBuilder.ForRepository.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF));
        given()
                .github(mocks -> {
                    var applicationClient = mocks.applicationClient();
                    {
                        // Scope: application client
                        var appMock = mocks.ghObject(GHApp.class, 1);
                        when(applicationClient.getApp()).thenReturn(appMock);
                        when(appMock.getSlug()).thenReturn(installationRef.appSlug());

                        var installationMock = Mockito.mock(GHAppInstallation.class);
                        when(installationMock.getId()).thenReturn(installationRef.installationId());
                        var installationsMocks = mockPagedIterable(installationMock);
                        when(appMock.listInstallations()).thenReturn(installationsMocks);
                    }

                    var installationClient = mocks.installationClient(installationRef.installationId());
                    {
                        // Scope: installation client
                        var installationMock = Mockito.mock(GHAuthenticatedAppInstallation.class);
                        when(installationClient.getInstallation()).thenReturn(installationMock);

                        var installationRepositoryMock = Mockito.mock(GHRepository.class);
                        var installationRepositoryMocks = mockPagedIterable(installationRepositoryMock);
                        when(installationMock.listRepositories()).thenReturn(installationRepositoryMocks);
                        when(installationRepositoryMock.getFullName()).thenReturn(repoRef.repositoryName());
                    }
                })
                .when(() -> {
                    MatcherAssert.assertThat(gitHubService.listRepositories(), Matchers.containsInAnyOrder(repoRef));
                })
                .then().github(mocks -> {
                    verifyNoMoreInteractions(queryIssuesBuilderMock);
                    verifyNoMoreInteractions(mocks.ghObjects());
                });
    }
}

