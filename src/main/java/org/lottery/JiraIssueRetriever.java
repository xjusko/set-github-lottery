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

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.set.aphrodite.spi.AphroditeException;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class JiraIssueRetriever {

    @ConfigProperty(name = "jira.password")
    String jiraPassword;

    public List<Issue> searchIssues() throws AphroditeException {
        IssueTrackerConfig jiraService = new IssueTrackerConfig("https://issues.redhat.com/", jiraPassword, TrackerType.JIRA, 200);

        List<IssueTrackerConfig> issueTrackerConfigs = new ArrayList<>();
        issueTrackerConfigs.add(jiraService);
        List<RepositoryConfig> repositoryConfigs = new ArrayList<>();
        List<StreamConfig> streamConfigs = new ArrayList<>();
        AphroditeConfig config = new AphroditeConfig(issueTrackerConfigs, repositoryConfigs, streamConfigs);

        Aphrodite aphrodite = Aphrodite.instance(config);
        SearchCriteria sc = new SearchCriteria.Builder()
                .setProduct("JBEAP")
                .setRelease(new Release("7.4.9.GA"))
                .build();
        return aphrodite.searchIssues(sc);
    }
}
