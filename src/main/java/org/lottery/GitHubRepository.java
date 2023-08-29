package org.lottery;

import io.quarkiverse.githubapp.ConfigFile;
import io.quarkiverse.githubapp.GitHubClientProvider;
import io.quarkiverse.githubapp.GitHubConfigFileProvider;
import io.quarkus.logging.Log;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.lottery.config.Config;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;

public class GitHubRepository implements AutoCloseable {

    private final GitHubClientProvider clientProvider;
    private final GitHubConfigFileProvider configFileProvider;
    private final GitHubRepositoryRef ref;

    private GitHub client;
    private GHRepository repository;
    private DynamicGraphQLClient graphQLClient;

    public GitHubRepository(GitHubClientProvider clientProvider, GitHubConfigFileProvider configFileProvider,
                            GitHubRepositoryRef ref) {
        this.clientProvider = clientProvider;
        this.configFileProvider = configFileProvider;
        this.ref = ref;
    }

    @Override
    public void close() {
        if (graphQLClient != null) {
            try {
                graphQLClient.close();
            } catch (Exception e) {
                Log.errorf(e, "Could not close GraphQL client");
            }
        }
    }

    public GitHubRepositoryRef ref() {
        return ref;
    }

    public String appLogin() {
        return ref.installationRef().appLogin();
    }

    private GitHub client() {
        if (client == null) {
            client = clientProvider.getInstallationClient(ref.installationRef().installationId());
        }
        return client;
    }

    private GHRepository repository() throws IOException {
        if (repository == null) {
            repository = client().getRepository(ref.repositoryName());
        }
        return repository;
    }

    private DynamicGraphQLClient graphQLClient() {
        if (graphQLClient == null) {
            graphQLClient = clientProvider.getInstallationGraphQLClient(ref.installationRef().installationId());
        }
        return graphQLClient;
    }

    public Optional<Config> fetchLotteryConfig() throws IOException {
        return configFileProvider.fetchConfigFile(repository(), Config.FILE_NAME, ConfigFile.Source.DEFAULT,
                Config.class);
    }

    public void commentOnIssue(Config.Participant participant, StringBuilder commentText) throws IOException {
        PagedIterable<GHIssue> githubIssues = repository().queryIssues().assignee(participant.user()).list();
        for (GHIssue issue : githubIssues) {
            issue.comment(commentText.toString());
        }
    }
}
