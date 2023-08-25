package org.lottery;
import io.quarkiverse.githubapp.ConfigFile;
import io.quarkiverse.githubapp.GitHubClientProvider;
import io.quarkiverse.githubapp.GitHubConfigFileProvider;
import org.jboss.set.aphrodite.domain.Issue;


import io.quarkus.scheduler.Scheduled;
import org.kohsuke.github.GHApp;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.lottery.config.Config;

import jakarta.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LotteryService {

    @Inject
    JiraIssueRetriever jiraIssueRetriever;
    @Inject
    GitHubService gitHubService;
    @Inject
    GitHubConfigFileProvider configFileProvider;

    @Scheduled(every = "1H", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public synchronized void commentOnIssue() throws Exception {


        for (GHRepository repository : gitHubService.listRepositories()) {
            List<Issue> issueList = jiraIssueRetriever.searchIssues();

            Optional<Config> configFile = configFileProvider.fetchConfigFile(repository, Config.FILE_NAME, ConfigFile.Source.DEFAULT, Config.class);
            if (configFile.isEmpty()) {
                return;
            }
            Config config = configFile.get();

            try {
                for (Config.Participant participant : config.participants()) {
                    StringBuilder commentText = new StringBuilder("Hey @" + participant.user() + ", here is your report on " + LocalDate.now() + ".\n");

                    int participantIssueCount = Math.min(participant.issueCount(), issueList.size());

                    // Get a list of issue URLs
                    List<String> issueUrls = getIssueUrls(issueList);

                    // Randomly select a subset of issue URLs
                    List<String> randomIssueUrls = getRandomIssueUrls(issueUrls, participantIssueCount);

                    for (String issueUrl : randomIssueUrls) {
                        commentText.append(issueUrl).append("\n");
                    }

                    if (!randomIssueUrls.isEmpty() && participant.isReminderDay() && participant.isReminderTime()) {
                        commentOnIssue(participant, repository, commentText);
                    }

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    private static void commentOnIssue(Config.Participant participant, GHRepository repository, StringBuilder commentText) throws IOException {
        PagedIterable<GHIssue> githubIssues = repository.queryIssues().assignee(participant.user()).list();
        for (GHIssue issue : githubIssues) {
            issue.comment(commentText.toString());
        }
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
