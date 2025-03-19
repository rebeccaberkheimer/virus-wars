package edu.commonwealthu.viruswars;

import static android.content.Context.MODE_PRIVATE;
import static edu.commonwealthu.viruswars.Occupant.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Front end for the game of Virus wars, dynamically updates based on the mode selected in the
 * Selection Fragment
 *
 * @author Rebecca Berkheimer
 */

public class GameFragment extends Fragment {

    private static final int INIT_SIZE = 8;
    private int currentSize = INIT_SIZE;
    private static final int INIT_MODE = 0; // 0 for Standard, 1 for Sudden Death, 2 for Challenge
    private int currentMode = INIT_MODE;
    private VirusWars game;
    private TextView modeText;
    private TextView turnText;
    private ImageButton[][] imageButtons;
    private SoundManager soundManager;
    private StatsDatabase db;
    private Animation move;

    private static final String GAME_STATE = "gameState";
    private static final String GAME_SIZE = "gridSize";
    private static final String GAME_MODE = "gameMode";
    private static final String NUM_MOVES = "moves";
    private static final String MOVE = "move";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        soundManager = new SoundManager(requireContext());
         db = Room.databaseBuilder(requireContext(), StatsDatabase.class,
        "StatsDatabase").fallbackToDestructiveMigration().build();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    /**
     * Initializes instance variables, creates a custom menu for the GameFragment, registers
     * event handlers for control buttons, and displays the grid in its starting configuration.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Update the toolbar for the Game Fragment, using MenuHost
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_game, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                final int id = menuItem.getItemId();
                if (id == R.id.menu_grids_grid6) {
                    handleOptionGrid6();
                }
                else if (id == R.id.menu_grids_grid7) {
                    handeOptionGrid7();
                }
                else if (id == R.id.menu_grids_grid8) {
                    handeOptionGrid8();
                }
                else if (id == R.id.menu_grids_grid10) {
                    handeOptionGrid10();
                }
                else if (id == R.id.menu_load_game) {
                    handleOptionLoad();
                }
                else if (id == R.id.menu_save_game) {
                    handleOptionSave();
                }
                else if (id == R.id.menu_sound) {
                    handleOptionSound(menuItem);
                }
                else if(id == R.id.menu_home) {
                    handleOptionHome();
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        // Get the current mode from the Selection Fragment, passed as an int
        currentMode = requireArguments().getInt("game_mode");
        modeText = view.findViewById(R.id.modeText);
        turnText = view.findViewById(R.id.turnText);
        move = AnimationUtils.loadAnimation(requireContext(), R.anim.move);
        if (savedInstanceState != null) {
            currentSize = savedInstanceState.getInt(GAME_SIZE, INIT_SIZE);
            currentMode = savedInstanceState.getInt(GAME_MODE, INIT_MODE);
            game = new VirusWars(currentSize, currentMode);

            // retrieve saved moves and make those moves in game
            int numMoves = savedInstanceState.getInt(NUM_MOVES, 0);
            for (int i = 0; i < numMoves; i++) {
                int x = savedInstanceState.getInt(MOVE + i + "0", 0);
                int y = savedInstanceState.getInt(MOVE + i + "1", 0);
                game.move(x, y);
            }
        } else {
            game = new VirusWars(INIT_SIZE, currentMode);
        }

        view.findViewById(R.id.undoButton).setOnClickListener(this::undo);
        view.findViewById(R.id.restartButton).setOnClickListener(this::restart);
        view.findViewById(R.id.infoButton).setOnClickListener(v ->
                showInformationDialog(view.getContext(), getString(R.string.information)));

        setImageButtons(view);
        drawBoard();
    }

    /**
     * Releases the sound manager data and closes the database connection when the Fragment is
     * destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        soundManager.release();
        db.close();
    }

    /**
     * Stores game state in a bundle.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (game != null) {
            outState.putInt(GAME_SIZE, game.size());
            outState.putInt(GAME_MODE, game.mode());
            outState.putInt(NUM_MOVES, game.moves());
            int[][] moveHistory = game.moveHistory();
            for (int i = 0; i < moveHistory.length; i++) {
                outState.putInt(MOVE + i + "0", moveHistory[i][0]);
                outState.putInt(MOVE + i + "1", moveHistory[i][1]);
            }
        }
    }

    /**
     * Assigns and places each image buttons event listener
     */
    private void setImageButtons(View view) {
        imageButtons = new ImageButton[currentSize][currentSize];

        GridLayout gridLayout = view.findViewById(R.id.grid_layout);
        gridLayout.removeAllViews();

        gridLayout.setRowCount(currentSize);
        gridLayout.setColumnCount(currentSize);
        Resources resources = getResources();
        int displayWidth = resources.getDisplayMetrics().widthPixels;
        int displayHeight = resources.getDisplayMetrics().heightPixels;
        int n = Math.min(displayWidth, displayHeight);
        int buttonSize = (8 * n / 10) / currentSize;

        // Create buttons and add them to the grid
        for (int i = 0; i < currentSize; i++) {
            for (int j = 0; j < currentSize; j++) {
                imageButtons[i][j] = new ImageButton(view.getContext());
                GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
                gridParams.width = buttonSize;
                gridParams.height = buttonSize;
                gridParams.setMargins(3, 3, 3, 3);

                imageButtons[i][j].setLayoutParams(gridParams);
                imageButtons[i][j].setBackgroundColor(ContextCompat.getColor(view.getContext(),
                        R.color.black));
                imageButtons[i][j].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageButtons[i][j].setTag(new int[] {i, j});
                imageButtons[i][j].setOnClickListener(this::move);
                gridLayout.addView(imageButtons[i][j]);
            }
        }
    }

    /**
     * Fills in the board with each image buttons corresponding image
     */
    private void drawBoard() {
        for(int i = 0; i < currentSize; i++) {
            for(int j = 0; j < currentSize; j++) {
                imageButtons[i][j].setBackgroundColor(ContextCompat.getColor(requireContext(),
                        R.color.grid_color));
                Occupant occupant = game.occupantAt(i, j);
                switch(occupant) {
                    case COLONY_X:
                        imageButtons[i][j].setImageResource(R.drawable.virus_red);
                        break;
                    case COLONY_O:
                        imageButtons[i][j].setImageResource(R.drawable.virus_blue2);
                        break;
                    case KILLED_X:
                        imageButtons[i][j].setImageResource(R.drawable.virus_red);
                        imageButtons[i][j].setBackgroundColor(ContextCompat.getColor
                                (requireContext(), R.color.x_killed_background));
                        break;
                    case KILLED_O:
                        imageButtons[i][j].setImageResource(R.drawable.virus_blue2);
                        imageButtons[i][j].setBackgroundColor(ContextCompat.getColor
                                (requireContext(), R.color.o_killed_background));
                        break;
                    case OBSTACLE:
                        imageButtons[i][j].setImageResource(R.drawable.obstacle2);
                        break;
                    default:
                        imageButtons[i][j].setImageResource(android.R.color.transparent);
                }
            }
        }
        setModeTextView(currentMode);
        setTurnTextView();
    }

    private void startGame() { // start a new game
        game = new VirusWars(currentSize, currentMode);
        setImageButtons(requireView()); // Ensures use of the root view to find the GridLayout
        drawBoard();
        soundManager.playStartSound();
    }

    /**
     * Makes a move based on the selected image button, if the game is won it will be saved
     * to the StatsDatabase
     */
    private void move(View view) {
        int[] a = (int[]) view.getTag();
        int x = a[0];
        int y = a[1];

        // Check if the position is valid and not a virus the player has already placed
        if (game.isAccessible(x, y)) {
            game.move(x, y);
            imageButtons[x][y].startAnimation(move);
            drawBoard();
            soundManager.playMoveSound();
            if(!game.canMove()) { // If a player cannot move, the game has been won
                soundManager.playWinSound();
                if (game.getCurrentPlayer() == COLONY_O) { // Turn has switched
                    turnText.setText(getString(R.string.colony_x_wins));
                    turnText.setTextColor(ContextCompat.getColor
                            (requireContext(), R.color.o_killed_background));
                }
                else {
                    turnText.setText(getString(R.string.colony_o_wins));
                    turnText.setTextColor(ContextCompat.getColor
                            (requireContext(), R.color.x_killed_background));
                }
                saveCompleteGame(); // Save the game to StatsDatabase once won
            }
        }
        else {
            soundManager.playErrorSound();
            showCustomToast(getString(R.string.invalid_move));
        }
    }

    private void setModeTextView(int m) {
        if (m == 0) {
            modeText.setText(getString(R.string.standard_mode_text));
        }
        else if (m == 1){
            modeText.setText(getString(R.string.sudden_death_mode));
        }
        else {
            modeText.setText(getString(R.string.challenge_mode_text));
        }
    }
    private void setTurnTextView() {
        if (game.getCurrentPlayer() == COLONY_O) {
            turnText.setText(getString(R.string.player_o));
            turnText.setTextColor(ContextCompat.
                    getColor(requireContext(), R.color.x_killed_background));
        }
        else {
            turnText.setText(getString(R.string.player_x));
            turnText.setTextColor(ContextCompat.
                    getColor(requireContext(), R.color.o_killed_background));
        }
    }

    /**
     * Undoes a move made by the user
     */
    private void undo(View view) {
        if(game.undo()) {
            soundManager.playUndoSound();
            drawBoard();
        }
        else {
            soundManager.playErrorSound();
            showCustomToast(getString(R.string.undo_fail));
        }
    }

    /**
     * Starts a new game based on the current mode and size
     */
    private void restart(View view) {
        if (game.moves() == 0) {
            soundManager.playErrorSound();
            showCustomToast(getString(R.string.restart_message));
        }
        else {
            startGame();
        }
    }

    /**
     * Displays a given message in a custom toast
     */
    private void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                requireActivity().findViewById(R.id.toast_layout_root));

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(requireContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showInformationDialog(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_generic, null);

        TextView messageTextView = dialogView.findViewById(R.id.generic_text_view);
        messageTextView.setText(message);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.toolbar_background);
        }
    }

    /**
     * Navigates the user from the GameFragment back to the SelectionFragment
     */
    private void handleOptionHome() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_game_to_selection);
    }

    private void handleOptionGrid6() {
        currentSize = 6;
        startGame();
    }

    private void handeOptionGrid7() {
        currentSize = 7;
        startGame();
    }

    private void handeOptionGrid8() {
        currentSize = 8;
        startGame();
    }

    private void handeOptionGrid10() {
        currentSize = 10;
        startGame();
    }

    private void handleOptionSound(MenuItem item) { // toggle sound effects
        if (soundManager.isSoundEnabled()) {
            item.setIcon(R.drawable.sound_off);
        } else {
            item.setIcon(R.drawable.sound_on);
        }
        soundManager.toggleSoundEnabled();
    }

    /**
     * Loads a game from SharedPreferences
     */
    private void handleOptionLoad() {
        SharedPreferences preferences =
                requireContext().getSharedPreferences(GAME_STATE, MODE_PRIVATE);
        if (preferences != null) {
            int size = preferences.getInt(GAME_SIZE, INIT_SIZE);
            int mode = preferences.getInt(GAME_MODE, INIT_MODE);
            int numMoves = preferences.getInt(NUM_MOVES, 0);

            currentSize = size;
            currentMode = mode;
            game = new VirusWars(size, mode);

            // retrieve saved moves and make those moves in game
            for (int i = 0; i < numMoves; i++) {
                int x = preferences.getInt(MOVE + i + "0", 0);
                int y = preferences.getInt(MOVE + i + "1", 0);
                game.move(x, y);
            }

            setImageButtons(requireView());
            drawBoard();
            soundManager.playStartSound();
        }
    }

    /**
     * Saves a game to SharedPreferences
     */
    private void handleOptionSave() {
        SharedPreferences preferences = requireContext().
                getSharedPreferences(GAME_STATE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        int size = game.size();
        int mode = game.mode();
        editor.putInt(GAME_SIZE, size);
        editor.putInt(GAME_MODE, mode);
        editor.putInt(NUM_MOVES, game.moves());

        // Store moves made in the game
        int[][] a = game.moveHistory();
        for (int i = 0; i < a.length; i++) {
            editor.putInt(MOVE + i + "0", a[i][0]);
            editor.putInt(MOVE + i + "1", a[i][1]);
        }
        editor.apply();
        showCustomToast(getString(R.string.save_finished_game));
    }

    /**
     * Saves a game once it has been won to the StatsDatabase
     */
    private void saveCompleteGame() {
        // Saves the game using an ExecutorService because Room does not allow updates to databases
        // on the main thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            CompletedGame completedGame = new CompletedGame(game.mode(), game.size(), game.moves(),
                    game.getCurrentPlayer().opposite());
            // Sets the gameID to the primary key automatically created when inserted into the table
            completedGame.gameID = (int) db.getStatsDAO().insertCompleteGame(completedGame);
        });

        // Shutdown the ExecutorService after each insertion
        executorService.shutdown();
    }
}
