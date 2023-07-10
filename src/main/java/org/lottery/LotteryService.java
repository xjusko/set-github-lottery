package org.lottery;
import io.quarkiverse.githubapp.ConfigFile;
import io.quarkiverse.githubapp.GitHubClientProvider;
import io.quarkiverse.githubapp.GitHubConfigFileProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.config.AphroditeConfig;
import org.jboss.set.aphrodite.config.IssueTrackerConfig;
import org.jboss.set.aphrodite.config.RepositoryConfig;
import org.jboss.set.aphrodite.config.StreamConfig;
import org.jboss.set.aphrodite.config.TrackerType;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.domain.Release;
import org.jboss.set.aphrodite.domain.SearchCriteria;
import org.jboss.set.aphrodite.repository.services.common.RepositoryType;


import io.quarkus.scheduler.Scheduled;
import org.kohsuke.github.GHApp;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.lottery.config.Config;

import jakarta.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LotteryService {

    @ConfigProperty(name = "github.password")
    String githubPassword;

    @ConfigProperty(name = "jira.password")
    String jiraPassword;

    @Inject
    GitHubConfigFileProvider configFileProvider;

    @Inject
    GitHubClientProvider gitHubClientProvider;

    @Scheduled(every = "1H", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public synchronized void commentOnIssue() throws Exception {

        RepositoryConfig githubService = new RepositoryConfig("https://github.com/", "xjusko", githubPassword, RepositoryType.GITHUB);

        IssueTrackerConfig jiraService =
                new IssueTrackerConfig("https://issues.redhat.com/", jiraPassword, TrackerType.JIRA, 200);

        List<IssueTrackerConfig> issueTrackerConfigs = new ArrayList<>();
        issueTrackerConfigs.add(jiraService);

        List<RepositoryConfig> repositoryConfigs = new ArrayList<>();
        //repositoryConfigs.add(githubService);

        List<StreamConfig> streamConfigs=new ArrayList<>();
        AphroditeConfig config = new AphroditeConfig(issueTrackerConfigs, repositoryConfigs, streamConfigs);

//        GitHub client = gitHubClientProvider.getApplicationClient();
//        GHApp app = client.getApp();
//        GHRepository repository = gitHubClientProvider.getInstallationClient(app.listInstallations().toList().get(0).getId()).getInstallation().listRepositories().toList().get(0);
//
//        Optional<Config> configFile = configFileProvider.fetchConfigFile(repository, Config.FILE_NAME, ConfigFile.Source.DEFAULT, Config.class);
//        configFile.ifPresent(value ->
//        {
//            for (Config.Participant participant: value.participants()) {
//                PagedIterable<GHIssue> queriedIssues = repository.queryIssues().assignee(participant.user()).list();
//                if (!queriedIssues.iterator().hasNext()) {
//                    try {
//                        GHIssueBuilder issueBuilder = repository.createIssue("title");
//                        issueBuilder.body(String.valueOf(participant.issueCount()));
//                        issueBuilder.assignee(participant.user());
//                        issueBuilder.create();
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                } else {
//                    for (GHIssue issue : queriedIssues) {
//
//                        try {
//                          issue.comment(String.valueOf(participant.issueCount()));
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//
//            }
//        });






        Aphrodite aphrodite = Aphrodite.instance(config) ;
        SearchCriteria sc = new SearchCriteria.Builder()
                .setProduct("JBEAP")
                .setRelease(new Release("7.4.9.GA"))
                .build();
        List<Issue> result = aphrodite.searchIssues(sc);


        StringBuilder issueComment = new StringBuilder();
        for (Issue url : result) {
            issueComment.append(url.getURL());
            issueComment.append("\n");
        }
        //System.out.println(issueComment);
        System.out.println(result.size());

    }

}
