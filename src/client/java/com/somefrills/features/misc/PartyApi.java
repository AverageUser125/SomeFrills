package com.somefrills.features.misc;

import com.somefrills.events.ChatMsgEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.somefrills.Main.mc;

public class PartyApi {

    private static final List<String> partyMembers = new ArrayList<>();

    private static String partyLeader = null;
    private static String previousLeader = null;

    // -------------------------
    // PUBLIC API
    // -------------------------

    public static List<String> getPartyMembers() {
        return Collections.unmodifiableList(partyMembers);
    }

    public static boolean isInParty() {
        return !partyMembers.isEmpty();
    }

    public static boolean isLeader() {
        String self = Utils.getPlayerName(mc.player);
        return self != null && self.equalsIgnoreCase(partyLeader);
    }

    public static String getPartyLeader() {
        return partyLeader;
    }

    public static boolean isMember(String name) {
        for (String member : partyMembers) {
            if (member.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    // -------------------------
    // CHAT PARSER
    // -------------------------

    @EventHandler
    private void onChatMessage(ChatMsgEvent event) {
        String myself = Utils.getPlayerName(mc.player);
        String msg = event.messagePlain;

        if (myself == null || msg == null || msg.isEmpty()) return;

        msg = msg.trim();

        // -------------------------
        // JOIN PARTY
        // -------------------------

        // You have joined X's party!
        if (msg.startsWith("You have joined ") && msg.endsWith("'s party!")) {
            String name = extractBetween(msg, "You have joined ", "'s party!");
            resetParty();
            partyLeader = cleanName(name);
            addMember(partyLeader, myself);
            return;
        }

        // X joined the party.
        if (msg.endsWith(" joined the party.")) {
            String name = msg.replace(" joined the party.", "");
            name = cleanName(name);

            if (partyMembers.isEmpty()) {
                partyLeader = myself;
            }

            addMember(name, myself);
            return;
        }

        // You'll be partying with: A, B, C
        if (msg.startsWith("You'll be partying with: ")) {
            String names = msg.substring("You'll be partying with: ".length());

            for (String part : names.split(", ")) {
                addMember(cleanName(part), myself);
            }
            return;
        }

        // -------------------------
        // LEAVE / REMOVE
        // -------------------------

        if (msg.endsWith(" has left the party.")) {
            removeMember(cleanName(msg.replace(" has left the party.", "")));
            return;
        }

        if (msg.endsWith(" has been removed from the party.")) {
            removeMember(cleanName(msg.replace(" has been removed from the party.", "")));
            return;
        }

        if (msg.startsWith("Kicked ") && msg.endsWith(" because they were offline.")) {
            String name = extractBetween(msg, "Kicked ", " because they were offline.");
            removeMember(cleanName(name));
            return;
        }

        if (msg.endsWith(" was removed from your party because they disconnected.")) {
            String name = msg.replace(" was removed from your party because they disconnected.", "");
            removeMember(cleanName(name));
            return;
        }

        // -------------------------
        // TRANSFER
        // -------------------------

        // The party was transferred to X because Y left
        if (msg.startsWith("The party was transferred to ")
                && msg.contains(" because ")
                && msg.endsWith(" left")) {

            String rest = msg.substring("The party was transferred to ".length());
            String[] split = rest.split(" because ");

            if (split.length == 2) {
                partyLeader = cleanName(split[0]);
                removeMember(cleanName(split[1].replace(" left", "")));
            }
            return;
        }

        // The party was transferred to X by Y
        if (msg.startsWith("The party was transferred to ")
                && msg.contains(" by ")) {

            String rest = msg.substring("The party was transferred to ".length());
            String[] split = rest.split(" by ");

            if (split.length == 2) {
                partyLeader = cleanName(split[0]);
                previousLeader = cleanName(split[1]);
            }
            return;
        }

        // -------------------------
        // DISBAND / LEFT PARTY
        // -------------------------

        if (msg.contains("has disbanded the party!")
                || msg.equals("You left the party.")
                || msg.equals("The party was disbanded because all invites expired and the party was empty.")
                || msg.equals("You are not currently in a party.")
                || msg.equals("You are not in a party.")
                || msg.equals("The party was disbanded because the party leader disconnected.")
                || msg.startsWith("You have been kicked from the party by ")) {

            resetParty();
            return;
        }

        // -------------------------
        // PARTY LIST COMMAND OUTPUT
        // -------------------------

        // Party Members (5)
        if (msg.startsWith("Party Members (")) {
            partyMembers.clear();
            return;
        }

        // Party Leader: X ●
        // Party Moderators: A ● B
        // Party Members: A ● B ● C
        if (msg.startsWith("Party ")) {
            parsePartyListLine(msg, myself);
        }
    }

    // -------------------------
    // HELPERS
    // -------------------------

    private static void parsePartyListLine(String line, String myself) {
        int colon = line.indexOf(": ");
        if (colon == -1) return;

        String left = line.substring(0, colon);
        String right = line.substring(colon + 2);

        boolean leaderLine = left.equals("Party Leader");

        String[] split = right.split(" ● ");

        for (String raw : split) {
            String name = cleanName(raw);
            if (name.isEmpty()) continue;

            addMember(name, myself);

            if (leaderLine) {
                partyLeader = name;
            }
        }
    }

    private static void addMember(String name, String myself) {
        if (name == null || name.isEmpty()) return;
        if (name.equalsIgnoreCase(myself)) return;
        if (isMember(name)) return;

        partyMembers.add(name);
    }

    private static void removeMember(String name) {
        partyMembers.removeIf(x -> x.equalsIgnoreCase(name));

        if (name.equalsIgnoreCase(previousLeader == null ? "" : previousLeader)) {
            previousLeader = null;
        }

        if (name.equalsIgnoreCase(partyLeader == null ? "" : partyLeader)) {
            partyLeader = null;
        }
    }

    private static void resetParty() {
        partyMembers.clear();
        partyLeader = null;
        previousLeader = null;
    }

    private static String extractBetween(String text, String start, String end) {
        if (!text.startsWith(start) || !text.endsWith(end)) return "";

        return text.substring(start.length(), text.length() - end.length());
    }

    private static String cleanName(String text) {
        if (text == null) return "";

        text = text.replace("●", "").trim();

        // remove rank prefixes like [MVP+]
        while (text.startsWith("[")) {
            int end = text.indexOf("]");
            if (end == -1) break;
            text = text.substring(end + 1).trim();
        }

        return text;
    }
}