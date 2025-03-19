package edu.commonwealthu.viruswars;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Main Activity for hosting each fragment and creating global menu options
 *
 * @author Rebecca Berkheimer
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> true);
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                splashScreen.setKeepOnScreenCondition(() -> false), 3000);
        setContentView(R.layout.activity_main);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.showOverflowMenu();
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleStyle);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_exit) {
            showExitDialog();
        }
        else if (id == R.id.menu_about) {
            showAboutDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_about, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.toolbar_background);
        }
    }
    protected void showExitDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_exit, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (v, n) -> finish());
        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.toolbar_background);
        }
    }

}