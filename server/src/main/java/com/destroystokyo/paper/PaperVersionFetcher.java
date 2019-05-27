package com.destroystokyo.paper;

import com.destroystokyo.paper.util.VersionFetcher;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.StreamSupport;

public class PaperVersionFetcher implements VersionFetcher {
    private static final java.util.regex.Pattern VER_PATTERN = java.util.regex.Pattern.compile("^([0-9\\.]*)\\-.*R"); // R is an anchor, will always give '-R' at end
    private static final String GITHUB_BRANCH_NAME = "master";
    private static final String DOWNLOAD_PAGE = "https://papermc.io/downloads";
    private static @Nullable String mcVer;

    @Override
    public long getCacheTime() {
        return 720000;
    }

    @Nonnull
    @Override
    public Component getVersionMessage(@Nonnull String serverVersion) {
        String[] parts = serverVersion.substring("git-Paper-".length()).split("[-\\s]");
        return getUpdateStatusMessage("PaperMC/Paper", GITHUB_BRANCH_NAME, parts[0]);
    }

    private static @Nullable String getMinecraftVersion() {
        if (mcVer == null) {
            java.util.regex.Matcher matcher = VER_PATTERN.matcher(org.bukkit.Bukkit.getBukkitVersion());
            if (matcher.find()) {
                String result = matcher.group();
                mcVer = result.substring(0, result.length() - 2); // strip 'R' anchor and trailing '-'
            } else {
                org.bukkit.Bukkit.getLogger().warning("Unable to match version to pattern! Report to PaperMC!");
                org.bukkit.Bukkit.getLogger().warning("Pattern: " + VER_PATTERN.toString());
                org.bukkit.Bukkit.getLogger().warning("Version: " + org.bukkit.Bukkit.getBukkitVersion());
            }
        }

        return mcVer;
    }

    private static Component getUpdateStatusMessage(@Nonnull String repo, @Nonnull String branch, @Nonnull String versionInfo) {
        int distance;
        try {
            int jenkinsBuild = Integer.parseInt(versionInfo);
            distance = fetchDistanceFromSiteApi(jenkinsBuild, getMinecraftVersion());
        } catch (NumberFormatException ignored) {
            versionInfo = versionInfo.replace("\"", "");
            distance = fetchDistanceFromGitHub(repo, branch, versionInfo);
        }

        switch (distance) {
            case -1:
                return Component.text("Error obtaining version information", NamedTextColor.YELLOW);
            case 0:
                return Component.text("You are running the latest version", NamedTextColor.GREEN);
            case -2:
                return Component.text("Unknown version", NamedTextColor.YELLOW);
            default:
                return Component.text("You are " + distance + " version(s) behind", NamedTextColor.YELLOW)
                        .append(Component.newline())
                        .append(Component.text("Download the new version at: ")
                                .append(Component.text(DOWNLOAD_PAGE, NamedTextColor.GOLD)
                                        .hoverEvent(Component.text("Click to open", NamedTextColor.WHITE))
                                        .clickEvent(ClickEvent.openUrl(DOWNLOAD_PAGE))));
        }
    }

    private static int fetchDistanceFromSiteApi(int jenkinsBuild, @Nullable String siteApiVersion) {
        if (siteApiVersion == null) { return -1; }
        try {
            try (BufferedReader reader = Resources.asCharSource(
                new URL("https://api.papermc.io/v2/projects/paper/versions/" + siteApiVersion),
                Charsets.UTF_8
            ).openBufferedStream()) {
                JsonObject json = new Gson().fromJson(reader, JsonObject.class);
                JsonArray builds = json.getAsJsonArray("builds");
                int latest = StreamSupport.stream(builds.spliterator(), false)
                    .mapToInt(e -> e.getAsInt())
                    .max()
                    .getAsInt();
                return latest - jenkinsBuild;
            } catch (JsonSyntaxException ex) {
                ex.printStackTrace();
                return -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Contributed by Techcable <Techcable@outlook.com> in GH-65
    private static int fetchDistanceFromGitHub(@Nonnull String repo, @Nonnull String branch, @Nonnull String hash) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.github.com/repos/" + repo + "/compare/" + branch + "..." + hash).openConnection();
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) return -2; // Unknown commit
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8))) {
                JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
                String status = obj.get("status").getAsString();
                switch (status) {
                    case "identical":
                        return 0;
                    case "behind":
                        return obj.get("behind_by").getAsInt();
                    default:
                        return -1;
                }
            } catch (JsonSyntaxException | NumberFormatException e) {
                e.printStackTrace();
                return -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
