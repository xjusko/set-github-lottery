package org.lottery;

import io.quarkiverse.githubapp.GitHubClientProvider;
import io.quarkiverse.githubapp.GitHubConfigFileProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kohsuke.github.GHApp;
import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class GitHubService {

    @Inject
    GitHubClientProvider gitHubClientProvider;

    public List<GHRepository> listRepositories() throws IOException {
        List<GHRepository> repositoryList = new ArrayList<>();
        GitHub client = gitHubClientProvider.getApplicationClient();
        GHApp app = client.getApp();
        String appSlug = app.getSlug();
        for (GHAppInstallation installation : app.listInstallations()) {
            long installationId = installation.getId();
            for (GHRepository repository : gitHubClientProvider.getInstallationClient(installationId).getInstallation().listRepositories()) {
                repositoryList.add(repository);
            }
        }
        return repositoryList;
    }

}
