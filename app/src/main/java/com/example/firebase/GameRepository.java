package com.example.firebase;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class GameRepository {

    private static final ArrayList<Game> arenaGames = new ArrayList<>();
    private static final ArrayList<Game> randomGames = new ArrayList<>();
    private static boolean isLoading = false;

    private static final String[] ARENA_NAMES = {
            "Elden Ring", "The Witcher 3", "Red Dead Redemption 2", "God of War", "Hades",
            "Hollow Knight", "Portal 2", "Stardew Valley", "Dark Souls III", "Celeste",
            "Doom Eternal", "Minecraft", "Sekiro Shadows Die Twice", "Ori and the Blind Forest",
            "Baldur's Gate 3", "Grand Theft Auto V", "The Last of Us Part II", "Persona 5 Royal",
            "Mass Effect Legendary Edition", "BioShock Infinite", "Super Mario Odyssey",
            "Uncharted 4", "Bloodborne", "Half-Life 2", "Skyrim", "Fallout New Vegas",
            "Outer Wilds", "It Takes Two", "Inside", "Final Fantasy VII Remake",
            "Resident Evil 4", "Metal Gear Solid V", "Divinity Original Sin 2", "Cuphead",
            "Returnal", "Forza Horizon 5", "Baldur's Gate II", "Street Fighter 6", "Cyberpunk 2077"
    };

    private static final String[] RANDOM_NAMES = {
            "Anthem", "No Man's Sky", "Gotham Knights", "Forspoken", "Redfall",
            "Babylon's Fall", "Marvel's Avengers", "Ride to Hell Retribution", "Sonic the Hedgehog 2006",
            "Tony Hawk's Pro Skater 5", "Aliens Colonial Marines", "The Day Before",
            "Big Rigs Over the Road Racing", "Daikatana", "The Quiet Man", "Gollum", "Left Alive",
            "Starfield", "Atomic Heart", "The Callisto Protocol", "Mass Effect Andromeda",
            "Saints Row", "Crackdown 3", "Biomutant", "Back 4 Blood", "Outriders",
            "Dead Island 2", "Ghostwire Tokyo", "Immortals of Aveum", "Scorn", "Maneater",
            "Balan Wonderworld", "Resident Evil Resistance", "Need for Speed Unbound", "Battlefield 2042",
            "Payday 3", "Skull and Bones", "Suicide Squad Kill the Justice League",
            "The Lord of the Rings Gollum", "Palworld", "Lords of the Fallen",
            "ARK Survival Ascended", "ARK Survival Evolved", "Watch Dogs Legion", "Far Cry 6",
            "Dragon Age The Veilguard", "Bleeding Edge", "LawBreakers", "Evolve",
            "Brink", "Battleborn", "Agents of Mayhem", "Homefront The Revolution",
            "Mafia III", "Mirror's Edge Catalyst", "Just Cause 4", "Rage 2",
            "ReCore", "Anthem Next", "Metal Gear Survive", "Bleak Faith Forsaken",
            "The Crew", "The Crew Motorfest", "Goat Simulator 3", "Crime Boss Rockay City",
            "Exoprimal", "Wild Hearts", "Remnant 2", "State of Decay 2",
            "Dying Light 2", "Marvel Midnight Suns", "Foamstars", "XDefiant"
    };

    public static ArrayList<Game> getArenaGames() { return arenaGames; }
    public static ArrayList<Game> getRandomGames() { return randomGames; }
    
    public static ArrayList<Game> getGames() {
        ArrayList<Game> all = new ArrayList<>(arenaGames);
        all.addAll(randomGames);
        return all;
    }

    public static Game getGame(int index) {
        ArrayList<Game> all = getGames();
        if (index < 0 || index >= all.size()) return null;
        return all.get(index);
    }

    public static void loadGames(Context context) {
        if (isLoading || (!arenaGames.isEmpty() && !randomGames.isEmpty())) return;
        isLoading = true;
        RequestQueue queue = Volley.newRequestQueue(context);

        for (String name : ARENA_NAMES) pullGame(context, queue, name, true);
        for (String name : RANDOM_NAMES) pullGame(context, queue, name, false);
    }

    private static void pullGame(Context context, RequestQueue queue, String gameName, boolean isArena) {
        String apiKey = BuildConfig.RAWG_API_KEY;
        String url = "https://api.rawg.io/api/games?key=" + apiKey + "&search=" + gameName.replace(" ", "%20") + "&page_size=1";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() > 0) {
                            JSONObject json = results.getJSONObject(0);
                            Game game = parseGame(json);
                            if (isArena) arenaGames.add(game); else randomGames.add(game);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }, error -> {});
        queue.add(request);
    }

    private static Game parseGame(JSONObject json) throws Exception {
        String name = json.optString("name", "Unknown");
        String img = json.optString("background_image", "");
        int meta = json.optInt("metacritic", 50);
        ArrayList<String> shots = new ArrayList<>();
        JSONArray arr = json.optJSONArray("short_screenshots");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) shots.add(arr.getJSONObject(i).optString("image"));
        }
        return new Game(name, img, meta, shots);
    }
}