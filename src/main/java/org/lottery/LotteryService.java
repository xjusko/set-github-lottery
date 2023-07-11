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
import org.jboss.set.aphrodite.spi.AphroditeException;
import org.kohsuke.github.GHApp;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.lottery.config.Config;

import jakarta.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
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

        IssueTrackerConfig jiraService =
                new IssueTrackerConfig("https://issues.redhat.com/", jiraPassword, TrackerType.JIRA, 200);

        List<IssueTrackerConfig> issueTrackerConfigs = new ArrayList<>();
        issueTrackerConfigs.add(jiraService);
        List<RepositoryConfig> repositoryConfigs = new ArrayList<>();
        List<StreamConfig> streamConfigs=new ArrayList<>();
        AphroditeConfig config = new AphroditeConfig(issueTrackerConfigs, repositoryConfigs, streamConfigs);

        Aphrodite aphrodite = Aphrodite.instance(config) ;
        SearchCriteria sc = new SearchCriteria.Builder()
                .setProduct("JBEAP")
                .setRelease(new Release("7.4.9.GA"))
                .build();
        List<Issue> issueList = aphrodite.searchIssues(sc);

        GitHub client = gitHubClientProvider.getApplicationClient();
        GHApp app = client.getApp();
        GHRepository repository = gitHubClientProvider.getInstallationClient(app.listInstallations().toList().get(0).getId()).getInstallation().listRepositories().toList().get(0);

        Optional<Config> configFile = configFileProvider.fetchConfigFile(repository, Config.FILE_NAME, ConfigFile.Source.DEFAULT, Config.class);
        configFile.ifPresent(value ->
        {
            try {
                for (Config.Participant participant : value.participants()) {
                    StringBuilder commentText = new StringBuilder("Hey @" + participant.user() + ", here is your report on " + LocalDate.now() + ".\n");
                    commentText.append(participant.days().size() + "\n");
                    commentText.append(participant.timezoneId()).append("\n");


                    int participantIssueCount = Math.min(participant.issueCount(), issueList.size());

                    // Get a list of issue URLs
                    List<String> issueUrls = getIssueUrls(issueList);

                    // Randomly select a subset of issue URLs
                    List<String> randomIssueUrls = getRandomIssueUrls(issueUrls, participantIssueCount);

                    for (String issueUrl : randomIssueUrls) {
                        commentText.append(issueUrl).append("\n");
                    }

                    if (randomIssueUrls.isEmpty()) {
                        commentText.append("No issues found.");
                    }

                    PagedIterable<GHIssue> githubIssues = repository.queryIssues().assignee(participant.user()).list();
                    for (GHIssue issue : githubIssues) {
                        issue.comment(commentText.toString());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });

    }

    // Helper method to retrieve a list of issue URLs
    private List<String> getIssueUrls(List<Issue> issueList) {
        List<String> issueUrls = new ArrayList<>();
        for (Issue issue : issueList) {
            issueUrls.add(String.valueOf(issue.getURL()));
        }
        return issueUrls;
    }

    // Helper method to retrieve a random subset of issue URLs
    private List<String> getRandomIssueUrls(List<String> issueUrls, int count) {
        List<String> randomIssueUrls = new ArrayList<>(issueUrls);
        Collections.shuffle(randomIssueUrls); // Randomize the order of issue URLs
        return randomIssueUrls.subList(0, count);
    }

}
