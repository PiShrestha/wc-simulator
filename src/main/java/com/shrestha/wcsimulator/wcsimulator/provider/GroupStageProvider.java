package com.shrestha.wcsimulator.wcsimulator.provider;

import com.shrestha.wcsimulator.wcsimulator.domain.Team;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GroupStageProvider {

        /**
         * Returns the raw group listing keyed by human-friendly labels like
         * "Group A", "Group B", ... with each value being the list of four
         * entries as provided (some entries are composite like
         * "Denmark/North Macedonia/..." to represent qualifiers).
         *
         * Kept for compatibility with older code and views.
         */
        public Map<String, List<String>> getGroups() {

                Map<String, List<String>> groups = new LinkedHashMap<>();

        groups.put("Group A", Arrays.asList(
                "Mexico",
                "South Africa",
                "South Korea",
                "Denmark/North Macedonia/Czech Republic/Ireland"
        ));

        groups.put("Group B", Arrays.asList(
                "Canada",
                "Italy/Northern Ireland/Wales/Bosnia and Herzegovina",
                "Qatar",
                "Switzerland"
        ));

        groups.put("Group C", Arrays.asList(
                "Brazil",
                "Morocco",
                "Haiti",
                "Scotland"
        ));

        groups.put("Group D", Arrays.asList(
                "United States",
                "Paraguay",
                "Australia",
                "Turkey/Romania/Slovakia/Kosovo"
        ));

        groups.put("Group E", Arrays.asList(
                "Germany",
                "Curaçao",
                "Ivory Coast",
                "Ecuador"
        ));

        groups.put("Group F", Arrays.asList(
                "Netherlands",
                "Japan",
                "Ukraine/Sweden/Poland/Albania",
                "Tunisia"
        ));

        groups.put("Group G", Arrays.asList(
                "Belgium",
                "Egypt",
                "Iran",
                "New Zealand"
        ));

        groups.put("Group H", Arrays.asList(
                "Spain",
                "Cape Verde",
                "Saudi Arabia",
                "Uruguay"
        ));

        groups.put("Group I", Arrays.asList(
                "France",
                "Senegal",
                "Bolivia/Suriname/Iraq",
                "Norway"
        ));

        groups.put("Group J", Arrays.asList(
                "Argentina",
                "Algeria",
                "Austria",
                "Jordan"
        ));

        groups.put("Group K", Arrays.asList(
                "Portugal",
                "New Caledonia/Jamaica/DR Congo",
                "Uzbekistan",
                "Colombia"
        ));

        groups.put("Group L", Arrays.asList(
                "England",
                "Croatia",
                "Ghana",
                "Panama"
        ));

        return groups;
    }

        /**
         * Beginner-friendly map keyed by the single-letter group id (A-L)
         * with the same four entries as strings (no object model).
         *
         * Example: "A" -> ["Mexico", "South Africa", "South Korea",
         * "Denmark/North Macedonia/Czech Republic/Ireland"]
         */
        public Map<String, List<String>> groupStrings() {
                Map<String, List<String>> out = new LinkedHashMap<>();
                getGroups().forEach((k, v) -> {
                        String group = normalizeKey(k);
                        out.put(group, new ArrayList<>(v));
                });
                return out;
        }

        /**
         * Build a complete groups map keyed by single-letter group IDs (A-L),
         * converting the static string lists into Team objects. Composite entries
         * like "Denmark/North Macedonia/..." are split into multiple Team options
         * to satisfy MVP "if a team can reach a slot, include it".
         */
        public Map<String, List<Team>> groups() {
                Map<String, List<Team>> out = new LinkedHashMap<>();
                getGroups().forEach((k, v) -> {
                        String group = normalizeKey(k);
                        List<Team> teams = new ArrayList<>();
                        for (String entry : v) {
                                for (String opt : splitComposite(entry)) {
                                        teams.add(team(opt, group));
                                }
                        }
                        out.put(group, teams);
                });
                return out;
        }

        private String normalizeKey(String key) {
                return key.startsWith("Group ") ? key.substring(6) : key;
        }

        private List<String> splitComposite(String entry) {
                String[] options = entry.split("/");
                List<String> out = new ArrayList<>(options.length);
                for (String opt : options) out.add(opt.trim());
                return out;
        }

        private Team team(String name, String group) {
                Team t = new Team();
                t.setName(name);
                t.setGroupId(group);
                t.setFifaRank(0);
                t.setPoints(0);
                return t;
        }
}

