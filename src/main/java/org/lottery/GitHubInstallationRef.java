package org.lottery;

public record GitHubInstallationRef(String appSlug, long installationId) {

    public String appLogin() {
        return appSlug() + "[bot]";
    }

}
