package org.lottery;
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

import java.util.ArrayList;
import java.util.List;

public class LotteryService {

    @ConfigProperty(name = "github.password")
    String githubPassword;

    @ConfigProperty(name = "jira.password")
    String jiraPassword;



    @Scheduled(every = "1H", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public synchronized void commentOnIssue() throws Exception {
        RepositoryConfig githubService = new RepositoryConfig("https://github.com/", "xjusko", githubPassword, RepositoryType.GITHUB);

        IssueTrackerConfig jiraService =
                new IssueTrackerConfig("https://issues.redhat.com/", jiraPassword, TrackerType.JIRA, 200);

        List<IssueTrackerConfig> issueTrackerConfigs = new ArrayList<>();
        issueTrackerConfigs.add(jiraService);

        List<RepositoryConfig> repositoryConfigs = new ArrayList<>();
        repositoryConfigs.add(githubService);

        List<StreamConfig> streamConfigs=new ArrayList<>();




        try (Aphrodite aphrodite = Aphrodite.instance(new AphroditeConfig(issueTrackerConfigs, repositoryConfigs, streamConfigs))) {
            SearchCriteria sc = new SearchCriteria.Builder()
                    .setProduct("JBEAP")
                    .setRelease(new Release("7.4.9.GA"))
                    .build();
            List<Issue> result = aphrodite.searchIssues(sc);
            System.out.println("kek");


//            String token = "***REMOVED***";
//            GitHub github = GitHub.connectUsingOAuth(token);
//
//            // The owner and name of the repository
//            String owner = "xjusko";
//            String repository = "lottery-bot-test";
//
//            // Get the repository
//            GHRepository repo = github.getRepository(owner + "/" + repository);
//
//            // Get the list of issues
//            List<GHIssue> issues = repo.getIssues(GHIssueState.OPEN);
//            var file = repo.getFileContent("lottery-config.yaml");



            StringBuilder issueComment = new StringBuilder();
            for (Issue url : result) {
                issueComment.append(url.getURL());
                issueComment.append("\n");
            }
            //System.out.println(issueComment);
            System.out.println(result.size());
        }
    }

}
