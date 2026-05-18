package com.somefrills.features.update

import com.google.gson.JsonPrimitive
import com.somefrills.utils.stripPrefix
import moe.nea.libautoupdate.GithubReleaseUpdateData
import moe.nea.libautoupdate.GithubReleaseUpdateSource
import moe.nea.libautoupdate.UpdateData
import kotlin.math.max

class CustomGithubReleaseUpdateSource(owner: String, repository: String) :
    GithubReleaseUpdateSource(owner, repository) {
    public override fun findLatestRelease(validReleases: Iterable<GithubRelease>): UpdateData? {
        var latest: GithubRelease? = null
        for (release in validReleases) {
            if (latest == null || compareVersions(release.getTagName(), latest.getTagName()) > 0) {
                latest = release
            }
        }
        checkNotNull(latest) { "No valid release found" }
        return findAsset(latest)
    }

    public override fun findAsset(release: GithubRelease?): UpdateData? {
        if (release == null) return null

        for (asset in release.getAssets()) {
            if (filterAsset(asset)) {
                return createReleaseData(asset, release)
            }
        }
        return null
    }

    private fun filterAsset(asset: GithubRelease.Download): Boolean {
        if (asset.getName() == null || asset.getBrowserDownloadUrl() == null) {
            return false
        }
        // Accept any jar file for the release
        return asset.getName().endsWith(".jar")
    }

    private fun createReleaseData(asset: GithubRelease.Download, release: GithubRelease): GithubReleaseUpdateData {
        val tagName = release.getTagName().stripPrefix("v")
        return GithubReleaseUpdateData(
            if (release.getName() != null) release.getName() else release.getTagName(),
            JsonPrimitive(tagName),
            null,
            asset.getBrowserDownloadUrl(),
            release.getBody(),
            release.getTargetCommitish(),
            release.getCreated_at(),
            release.getPublishedAt(),
            release.getHtmlUrl()
        )
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1: Array<String> = v1.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val parts2: Array<String> = v2.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (i in 0..<max(parts1.size, parts2.size)) {
            val p1 = if (i < parts1.size) parseVersionPart(parts1[i]) else 0
            val p2 = if (i < parts2.size) parseVersionPart(parts2[i]) else 0
            if (p1 != p2) {
                return p1.compareTo(p2)
            }
        }
        return 0
    }

    private fun parseVersionPart(part: String): Int {
        return try {
            part.replace("[^0-9]".toRegex(), "").toInt()
        } catch (_: NumberFormatException) {
            0
        }
    }
}
