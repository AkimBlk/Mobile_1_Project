package com.example.firebase;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment {

    private static final int MAX_SPAWNED = 3;
    private static final long SPAWN_INTERVAL_MS  = 10_000;
    private static final long DESPAWN_DELAY_MS = 60_000;
    private static final float CAPTURE_RADIUS_M = 50;
    private static final double SPAWN_RADIUS_DEG = 0.0018;
    private static final int MARKER_SIZE_PX = 160;
    private static final String CHANNEL_ID = "spawn_channel";
    private static final int NOTIF_ID  = 1;

    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private ItemizedIconOverlay<OverlayItem> mOverlay;
    private ItemizedIconOverlay<OverlayItem> arenaOverlay;
    private RadiusOverlay radiusOverlay;

    private final List<OverlayItem> items = new ArrayList<>();
    private final List<Target> strongTargets = new ArrayList<>();
    private final Map<OverlayItem, Runnable> despawnTimers = new HashMap<>();
    private final Handler handler = new Handler();
    private boolean spawnLoopRunning = false;

    private final Runnable spawnLoop = new Runnable() {
        @Override public void run() {
            if (myLocationOverlay != null) {
                GeoPoint pos = myLocationOverlay.getMyLocation();
                trySpawnOne(pos);
                map.invalidate();
            }
            handler.postDelayed(this, SPAWN_INTERVAL_MS);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
        }, 1);

        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));

        map = view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        mapController = map.getController();
        mapController.setZoom(19.0);

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), map);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        map.getOverlays().add(myLocationOverlay);

        radiusOverlay = new RadiusOverlay(CAPTURE_RADIUS_M, myLocationOverlay);
        map.getOverlays().add(radiusOverlay);

        createNotificationChannel();

        mOverlay = new ItemizedIconOverlay<>(new ArrayList<>(),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        if (checkDistance(item)) {
                            Runnable pending = despawnTimers.remove(item);
                            if (pending != null) handler.removeCallbacks(pending);
                            items.remove(item);
                            mOverlay.removeItem(item);
                            map.invalidate();

                            Intent intent = new Intent(getActivity(), GameActivity.class);
                            intent.putExtra("GAME_INDEX", Integer.parseInt(item.getSnippet()));
                            startActivity(intent);
                        }
                        return true;
                    }
                    @Override public boolean onItemLongPress(int index, OverlayItem item) { return false; }
                }, getContext());
        map.getOverlays().add(mOverlay);

        arenaOverlay = new ItemizedIconOverlay<>(new ArrayList<>(),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        if (checkDistance(item)) {
                            Intent intent = new Intent(getActivity(), ArenaLobbyActivity.class);
                            intent.putExtra("ARENA_INDEX", Integer.parseInt(item.getSnippet()));
                            startActivity(intent);
                        }
                        return true;
                    }
                    @Override public boolean onItemLongPress(int index, OverlayItem item) { return false; }
                }, getContext());
        map.getOverlays().add(arenaOverlay);

        view.findViewById(R.id.btn_recenter).setOnClickListener(v -> {
            if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
                myLocationOverlay.enableFollowLocation();
                mapController.animateTo(myLocationOverlay.getMyLocation());
            }
        });

        view.findViewById(R.id.btn_chat).setOnClickListener(v ->
                new ChatFragment().show(getParentFragmentManager(), "chat")
        );

        waitForGpsThenStart();
        return view;
    }

    private boolean checkDistance(OverlayItem item) {
        GeoPoint myPos = myLocationOverlay.getMyLocation();
        if (myPos == null) return false;

        GeoPoint itemPos = (GeoPoint) item.getPoint();
        float[] result = new float[1];
        android.location.Location.distanceBetween(
                myPos.getLatitude(), myPos.getLongitude(),
                itemPos.getLatitude(), itemPos.getLongitude(), result);

        if (result[0] > CAPTURE_RADIUS_M) {
            Toast.makeText(getContext(), "Trop loin ! Approche-toi à moins de " + (int) CAPTURE_RADIUS_M + " m.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private static class RadiusOverlay extends Overlay {
        private final float radiusMeters;
        private final MyLocationNewOverlay locationOverlay;
        private final Paint fillPaint;
        private final Paint strokePaint;

        RadiusOverlay(float radiusMeters, MyLocationNewOverlay locationOverlay) {
            this.radiusMeters = radiusMeters;
            this.locationOverlay = locationOverlay;
            fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            fillPaint.setColor(Color.argb(40, 0, 150, 255));
            fillPaint.setStyle(Paint.Style.FILL);
            strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            strokePaint.setColor(Color.argb(180, 0, 100, 255));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(3);
        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (shadow) return;
            GeoPoint center = locationOverlay.getMyLocation();
            if (center == null) return;
            android.graphics.Point centerPx = mapView.getProjection().toPixels(center, null);
            GeoPoint edgePoint = new GeoPoint(center.getLatitude() + (radiusMeters / 111320.0), center.getLongitude());
            android.graphics.Point edgePx = mapView.getProjection().toPixels(edgePoint, null);
            float radiusPx = Math.abs(centerPx.y - edgePx.y);
            canvas.drawCircle(centerPx.x, centerPx.y, radiusPx, fillPaint);
            canvas.drawCircle(centerPx.x, centerPx.y, radiusPx, strokePaint);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopSpawnLoop();
        handler.removeCallbacksAndMessages(null);
        despawnTimers.clear();
        items.clear();
        mOverlay = null;
        arenaOverlay = null;
        radiusOverlay = null;
        map = null;
    }

    private void waitForGpsThenStart() {
        if (myLocationOverlay != null
                && myLocationOverlay.getMyLocation() != null
                && !GameRepository.getArenaGames().isEmpty()
                && !GameRepository.getRandomGames().isEmpty()) {
            mapController.setCenter(myLocationOverlay.getMyLocation());
            displayArenas();
            startSpawnLoop();
        } else {
            handler.postDelayed(this::waitForGpsThenStart, 500);
        }
    }

    private void displayArenas() {
        List<Arena> arenas = ArenaRepository.getArenas();
        for (int i = 0; i < arenas.size(); i++) {
            Arena arena = arenas.get(i);
            OverlayItem arenaItem = new OverlayItem(arena.getName(), String.valueOf(i),
                    new GeoPoint(arena.getLatitude(), arena.getLongitude()));
            arenaOverlay.addItem(arenaItem);
            loadGameMarker(arenaItem, arena.getGame(), new GameMarkerTransform(Color.parseColor("#FFD700"), 10, "arena"));
        }
        map.invalidate();
    }

    private void trySpawnOne(GeoPoint center) {
        if (items.size() >= MAX_SPAWNED) return;

        List<Game> randomGames = GameRepository.getRandomGames();
        List<Game> allGames = GameRepository.getGames();

        HashSet<String> namesOnMap = new HashSet<>();
        for (OverlayItem item : items) namesOnMap.add(item.getTitle());

        List<Game> candidates = new ArrayList<>();
        for (Game g : randomGames) {
            if (!g.isCaptured() && !namesOnMap.contains(g.getName())) candidates.add(g);
        }
        if (candidates.isEmpty()) return;

        Game game = candidates.get((int) (Math.random() * candidates.size()));
        int globalIdx = allGames.indexOf(game);

        double lat = center.getLatitude()  + (Math.random() - 0.5) * SPAWN_RADIUS_DEG;
        double lon = center.getLongitude() + (Math.random() - 0.5) * SPAWN_RADIUS_DEG;

        OverlayItem newItem = new OverlayItem(game.getName(), String.valueOf(globalIdx), new GeoPoint(lat, lon));
        items.add(newItem);
        mOverlay.addItem(newItem);
        loadGameMarker(newItem, game, new GameMarkerTransform(Color.WHITE, 6, "classic"));
        scheduleDespawn(newItem);

        sendSpawnNotif(game.getName());
    }

    private void scheduleDespawn(OverlayItem item) {
        Runnable despawn = () -> {
            items.remove(item);
            if (mOverlay != null) mOverlay.removeItem(item);
            despawnTimers.remove(item);
            if (map != null) map.invalidate();
        };
        despawnTimers.put(item, despawn);
        handler.postDelayed(despawn, DESPAWN_DELAY_MS);
    }

    private void loadGameMarker(OverlayItem item, Game game, com.squareup.picasso.Transformation transformation) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (map == null) return;
                item.setMarker(new BitmapDrawable(getResources(), bitmap));
                map.invalidate();
                strongTargets.remove(this);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) { strongTargets.remove(this); }
            @Override public void onPrepareLoad(Drawable p) {}
        };
        strongTargets.add(target);
        Picasso.get().load(game.getImageUrl()).resize(MARKER_SIZE_PX, MARKER_SIZE_PX).centerCrop().transform(transformation).into(target);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Apparitions de jeux", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = requireContext().getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void sendSpawnNotif(String gameName) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        NotificationManagerCompat.from(requireContext()).notify(NOTIF_ID,
                new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Un jeu est apparu !")
                        .setContentText(gameName + " est près de toi !")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build());
    }

    private void startSpawnLoop() {
        if (spawnLoopRunning) return;
        spawnLoopRunning = true;
        handler.post(spawnLoop);
    }

    private void stopSpawnLoop() {
        spawnLoopRunning = false;
        handler.removeCallbacks(spawnLoop);
    }

    @Override public void onPause() { super.onPause(); if (map != null) map.onPause(); stopSpawnLoop(); }
    @Override public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) startSpawnLoop();
    }

    public static class GameMarkerTransform implements com.squareup.picasso.Transformation {
        private final int borderColor;
        private final int borderWidth;
        private final String key;

        public GameMarkerTransform(int borderColor, int borderWidth, String key) {
            this.borderColor = borderColor;
            this.borderWidth = borderWidth;
            this.key = key;
        }

        @Override
        public Bitmap transform(Bitmap source) {

            //ping circulaire
            int size = Math.min(source.getWidth(), source.getHeight());
            Bitmap squared = Bitmap.createBitmap(source, (source.getWidth()-size)/2, (source.getHeight()-size)/2, size, size);
            Bitmap output = Bitmap.createBitmap(size, size, source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setShader(new android.graphics.BitmapShader(squared, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP));
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
            border.setColor(borderColor);
            border.setStyle(Paint.Style.STROKE);
            border.setStrokeWidth(borderWidth);
            canvas.drawCircle(r, r, r - (borderWidth / 2f), border);

            if (source != output) source.recycle();
            return output;
        }

        @Override public String key() { return key; }
    }
}