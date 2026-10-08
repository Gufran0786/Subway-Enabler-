package com.gufran.subwayenabler;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView txtStatus;
    private Handler handler;

    // Toggle state keys — inhi se Prefs me save hoga
    private static final String K_CHARACTERS = "characters";
    private static final String K_BOARDS     = "boards";
    private static final String K_UPGRADES   = "upgrades";
    private static final String K_COINS      = "coins";
    private static final String K_KEYS       = "keys";
    private static final String K_FRAMES     = "frames";
    private static final String K_PROFILES   = "profiles";
    private static final String K_TROPHIES   = "trophies";
    private static final String K_PROXY      = "proxy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txtStatus);
        handler = new Handler(Looper.getMainLooper());

        // Pehli baar app kholte hi original files backup karo
        if (!Prefs.firstRunDone(this)) {
            new Thread(() -> {
                FileOps.ensureBaseDirs();
                FileOps.backupOriginalOnce();
                Prefs.markFirstRun(MainActivity.this);
                runOnUiThread(() -> showToast("Original files backed up", true));
            }).start();
        } else {
            FileOps.ensureBaseDirs();
        }

        LinearLayout cInv = findViewById(R.id.containerInventory);
        LinearLayout cCur = findViewById(R.id.containerCurrency);
        LinearLayout cCos = findViewById(R.id.containerCosmetics);
        LinearLayout cMas = findViewById(R.id.containerMaster);
        LinearLayout cPro = findViewById(R.id.containerProfile);

        /* ============ INVENTORY ============ */
        ToggleButtonView.build(this, cInv, R.drawable.ic_characters,
            "Characters Unlock", "Sabhi characters free",
            Prefs.isEnabled(this, K_CHARACTERS),
            on -> handleToggle(K_CHARACTERS, "characters_inventory.json",
                "Characters", on));

        ToggleButtonView.build(this, cInv, R.drawable.ic_boards,
            "Hoverboards Unlock", "Sabhi boards free",
            Prefs.isEnabled(this, K_BOARDS),
            on -> handleToggle(K_BOARDS, "boards_inventory.json",
                "Hoverboards", on));

        ToggleButtonView.build(this, cInv, R.drawable.ic_upgrades,
            "Upgrades Max", "Saare upgrades max",
            Prefs.isEnabled(this, K_UPGRADES),
            on -> handleToggle(K_UPGRADES, "upgrades_inventory.json",
                "Upgrades", on));

        /* ============ CURRENCY ============ */
        ToggleButtonView.build(this, cCur, R.drawable.ic_coins,
            "Coins Hack", "Coins max",
            Prefs.isEnabled(this, K_COINS),
            on -> handleToggle(K_COINS, "save.json",
                "Coins", on));

        ToggleButtonView.build(this, cCur, R.drawable.ic_keys,
            "Keys Hack", "Keys max",
            Prefs.isEnabled(this, K_KEYS),
            on -> handleToggle(K_KEYS, "save.json",
                "Keys", on));

        /* ============ COSMETICS ============ */
        ToggleButtonView.build(this, cCos, R.drawable.ic_frames,
            "Frames Unlock", "Sabhi frames",
            Prefs.isEnabled(this, K_FRAMES),
            on -> handleToggle(K_FRAMES, "frames.json",
                "Frames", on));

        ToggleButtonView.build(this, cCos, R.drawable.ic_profiles,
            "Profiles Unlock", "Sabhi profiles",
            Prefs.isEnabled(this, K_PROFILES),
            on -> handleToggle(K_PROFILES, "profiles.json",
                "Profiles", on));

        ToggleButtonView.build(this, cCos, R.drawable.ic_trophies,
            "Trophies Max", "Sabhi trophies",
            Prefs.isEnabled(this, K_TROPHIES),
            on -> handleToggle(K_TROPHIES, "trophies.json",
                "Trophies", on));

        /* ============ MASTER ============ */
        ToggleButtonView.build(this, cMas, R.drawable.ic_proxy,
            "Proxy Unlock", "Poora profile replace",
            Prefs.isEnabled(this, K_PROXY),
            this::handleProxyToggle);

        /* ============ PROFILE ============ */
        ToggleButtonView.build(this, cPro, R.drawable.ic_backup,
            "Backup Save", "Manual backup",
            false,
            on -> {
                new Thread(() -> {
                    String name = FileOps.manualBackup();
                    runOnUiThread(() -> {
                        if (name != null) showToast("Backup: " + name, true);
                        else showToast("Backup failed", false);
                    });
                }).start();
            });

        ToggleButtonView.build(this, cPro, R.drawable.ic_restore,
            "Restore Backup", "Latest restore",
            false,
            on -> {
                new Thread(() -> {
                    boolean ok = FileOps.restoreLatest();
                    runOnUiThread(() -> showToast(
                        ok ? "Restore done" : "Restore failed", ok));
                }).start();
            });
    }

    /* ========== TOGGLE HANDLERS ========== */

    private void handleToggle(String key, String fileName,
                              String label, boolean enable) {
        showToast(label + " " + (enable ? "Enable" : "Disable"), enable);

        new Thread(() -> {
            boolean ok;
            if (enable) {
                ok = FileOps.enableFile(fileName);
            } else {
                ok = FileOps.disableFile(fileName);
            }

            Prefs.setEnabled(MainActivity.this, key, enable);

            final boolean success = ok;
            runOnUiThread(() -> {
                if (success) {
                    setStatus("✅ " + label + (enable ? " Enabled" : " Disabled"), true);
                } else {
                    setStatus("❌ " + label + " failed — file missing?", false);
                }
            });
        }).start();
    }

    private void handleProxyToggle(boolean enable) {
        showToast("Proxy " + (enable ? "Enable" : "Disable"), enable);

        new Thread(() -> {
            boolean ok = enable ? FileOps.enableProxy() : FileOps.disableProxy();
            Prefs.setEnabled(MainActivity.this, K_PROXY, enable);

            final boolean success = ok;
            runOnUiThread(() -> {
                if (success) {
                    setStatus("✅ Full Profile " + (enable ? "Unlocked" : "Restored"), true);
                } else {
                    setStatus("❌ Proxy failed", false);
                }
            });
        }).start();
    }

    /* ========== UI HELPERS ========== */

    private void showToast(String msg, boolean enable) {
        handler.post(() -> {
            Toast t = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
            View v = t.getView();
            if (v != null) {
                v.setBackgroundColor(Color.parseColor(
                    enable ? "#16A34A" : "#DC2626"));
            }
            t.show();
        });
    }

    private void setStatus(String msg, boolean success) {
        handler.post(() -> {
            txtStatus.setText(msg);
            txtStatus.setTextColor(success
                ? Color.parseColor("#22C55E")
                : Color.parseColor("#EF4444"));
            txtStatus.setBackgroundColor(success
                ? Color.parseColor("#0D3D1F")
                : Color.parseColor("#3D0D0D"));
            txtStatus.setVisibility(View.VISIBLE);
            handler.postDelayed(() -> txtStatus.setVisibility(View.GONE), 3500);
        });
    }
  }
