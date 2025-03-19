package edu.commonwealthu.viruswars;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Screen for displaying winning and game statistics to the player
 *
 * @author Rebecca Berkheimer
 */
public class StatisticsFragment extends Fragment {
    private LinearLayout statisiticsLinearLayout;
    private TextView gameCountText;
    private TextView moveCountText;
    private StatsDatabase db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = Room.databaseBuilder(requireContext(), StatsDatabase.class,
                "StatsDatabase").fallbackToDestructiveMigration().build();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Creates a menu option for allowing the user to go back to the Selection fragment
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_statistics, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_home) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_statistics_to_selection);
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        gameCountText = view.findViewById(R.id.gameCountText);
        moveCountText = view.findViewById(R.id.moveCountText);
        statisiticsLinearLayout = view.findViewById(R.id.statisticsLinearLayout);
        loadWinStats();
    }

    private void loadWinStats() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            int gameCount = db.getStatsDAO().getTotalGamesPlayed();
            int moveCount = db.getStatsDAO().getAverageMoves();
            List<WinStats> stats = db.getStatsDAO().getPlayerWins();

            // Return to the main thread once the data has been queried
            requireActivity().runOnUiThread(() -> {
                gameCountText.setText(String.valueOf(gameCount));
                moveCountText.setText(String.valueOf(moveCount));
                // Add TextViews for each player
                for (WinStats stat : stats) {
                    TextView playerTextView = new TextView(getContext());
                    String winningText = String.format("%s: %d wins", stat.getWinner(), stat.getWinCount());
                    playerTextView.setText(winningText);
                    playerTextView.setTextAppearance(R.style.StatOptionTextStyle);
                    statisiticsLinearLayout.addView(playerTextView);
                }
            });

        });
        executorService.shutdown();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        db.close();
    }

}
