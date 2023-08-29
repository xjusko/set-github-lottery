package org.lottery;

public record GitHubRepositoryRef(GitHubInstallationRef installationRef, String repositoryName) {

    @Override
    public String toString() {
        return repositoryName + "(through " + installationRef + ")";
    }
}
