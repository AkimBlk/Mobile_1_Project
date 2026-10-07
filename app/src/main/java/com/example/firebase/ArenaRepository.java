package com.example.firebase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArenaRepository {
    private static final List<Arena> arenas = new ArrayList<>();

    public static List<Arena> getArenas() {
        if (arenas.isEmpty()) {
            List<Game> legendaries = GameRepository.getArenaGames();
            
            if (!legendaries.isEmpty()) {
                List<Game> shuffled = new ArrayList<>(legendaries);
                Collections.shuffle(shuffled);

                arenas.add(new Arena("Arène 1", 50.8210, 4.3930, shuffled.get(0 % shuffled.size())));
                arenas.add(new Arena("Arène 2", 50.8215, 4.3895, shuffled.get(1 % shuffled.size())));
                arenas.add(new Arena("Arène 3", 50.8225, 4.3880, shuffled.get(2 % shuffled.size())));
                arenas.add(new Arena("Arène 4", 50.8195, 4.3910, shuffled.get(3 % shuffled.size())));
                arenas.add(new Arena("Arène 5", 50.8200, 4.3940, shuffled.get(4 % shuffled.size())));

                arenas.add(new Arena("Arène 6", 50.8654, 4.4172, shuffled.get(5 % shuffled.size())));
                arenas.add(new Arena("Arène 7", 50.8660, 4.4185, shuffled.get(6 % shuffled.size())));
            }
        }
        return arenas;
    }
}