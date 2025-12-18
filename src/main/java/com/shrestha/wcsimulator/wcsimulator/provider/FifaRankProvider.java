package com.shrestha.wcsimulator.wcsimulator.provider;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FifaRankProvider {
    private final Map<String, Integer> ranks = new HashMap<>();

    public FifaRankProvider() {
        // Top 50
        put("Spain", 1);
        put("Argentina", 2);
        put("France", 3);
        put("England", 4);
        put("Brazil", 5);
        put("Portugal", 6);
        put("Netherlands", 7);
        put("Belgium", 8);
        put("Germany", 9);
        put("Croatia", 10);
        put("Morocco", 11);
        put("Italy", 12);
        put("Colombia", 13);
        put("USA", 14);
        put("United States", 14);
        put("Mexico", 15);
        put("Uruguay", 16);
        put("Switzerland", 17);
        put("Japan", 18);
        put("Senegal", 19);
        put("Iran", 20);
        put("Denmark", 21);
        put("South Korea", 22);
        put("Ecuador", 23);
        put("Austria", 24);
        put("Türkiye", 25);
        put("Turkey", 25);
        put("Australia", 26);
        put("Canada", 27);
        put("Ukraine", 28);
        put("Norway", 29);
        put("Panama", 30);
        put("Poland", 31);
        put("Wales", 32);
        put("Russia", 33);
        put("Egypt", 34);
        put("Algeria", 35);
        put("Scotland", 36);
        put("Serbia", 37);
        put("Nigeria", 38);
        put("Paraguay", 39);
        put("Tunisia", 40);
        put("Hungary", 41);
        put("Ivory Coast", 42);
        put("Cote d'Ivoire", 42);
        put("Sweden", 43);
        put("Czechia", 44);
        put("Czech Republic", 44);
        put("Slovakia", 45);
        put("Greece", 46);
        put("Romania", 47);
        put("Venezuela", 48);
        put("Costa Rica", 49);
        put("Uzbekistan", 50);
        // Mentions / qualified
        put("Qatar", 51);
        put("Saudi Arabia", 60);
        put("South Africa", 61);
        put("Jordan", 66);
        put("Cape Verde", 68);
        put("Cabo Verde", 68);
        put("Ghana", 72);
        put("Curaçao", 82);
        put("Curacao", 82);
        put("Haiti", 84);
        put("New Zealand", 86);
    }

    private void put(String name, int rank) { ranks.put(name.toLowerCase(), rank); }

    public int rankOf(String name) {
        if (name == null) return 999;
        return ranks.getOrDefault(name.toLowerCase(), 999);
    }
}
