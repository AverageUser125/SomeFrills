package com.somefrills.features.update;

import com.google.gson.JsonPrimitive;
import com.somefrills.misc.Utils;
import moe.nea.libautoupdate.GithubReleaseUpdateData;
import moe.nea.libautoupdate.GithubReleaseUpdateSource;
import moe.nea.libautoupdate.UpdateData;

public class CustomGithubReleaseUpdateSource extends GithubReleaseUpdateSource {
    public CustomGithubReleaseUpdateSource(String owner, String repository) {
        super(owner, repository);
    }

    @Override
    public UpdateData findLatestRelease(Iterable<GithubRelease> validReleases) {
        GithubRelease latest = null;
        for (GithubRelease release : validReleases) {
            if (latest == null || compareVersions(release.getTagName(), latest.getTagName()) > 0) {
                latest = release;
            }
        }
        if (latest == null) {
            throw new IllegalStateException("No valid release found");
        }
        return findAsset(latest);
    }

    @Override
    public UpdateData findAsset(GithubRelease release) {
        if (release == null) return null;

        for (GithubRelease.Download asset : release.getAssets()) {
            if (filterAsset(asset)) {
                return createReleaseData(asset, release);
            }
        }
        return null;
    }

    private boolean filterAsset(GithubRelease.Download asset) {
        if (asset.getName() == null || asset.getBrowserDownloadUrl() == null) {
            return false;
        }
        // Accept any jar file for the release
        return asset.getName().endsWith(".jar");
    }

    private GithubReleaseUpdateData createReleaseData(GithubRelease.Download asset, GithubRelease release) {
        String tagName = Utils.stripPrefix(release.getTagName(), "v");
        return new GithubReleaseUpdateData(
                release.getName() != null ? release.getName() : release.getTagName(),
                new JsonPrimitive(tagName),
                null,
                asset.getBrowserDownloadUrl(),
                release.getBody(),
                release.getTargetCommitish(),
                release.getCreated_at(),
                release.getPublishedAt(),
                release.getHtmlUrl()
        );
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
            int p1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int p2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;
            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
        }
        return 0;
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
