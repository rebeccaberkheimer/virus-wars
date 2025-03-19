package edu.commonwealthu.viruswars;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

/**
 * Starting screen for the Virus Wars app. Users can swipe through a ViewPager2 to view
 * previews of the different screens they can go to.
 *
 * @author Rebecca Berkheimer
 */
public class SelectionFragment extends Fragment {

    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selection, container, false);
    }

    /**
     * Initializes each PreviewOption and sets the PreviewAdapter, registers the event handler
     * for the button allowing navigation to other screen to occur. Navigates based on the
     * selected PreviewOption.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.view_pager);
        Button selectButton = view.findViewById(R.id.selection_button);
        ArrayList<PreviewOption> previews = new ArrayList<>();
        previews.add(new PreviewOption(R.string.standard_mode_text, R.string.standard_mode_description,
                R.drawable.standard_screenshot));
        previews.add(new PreviewOption(R.string.sudden_death_mode, R.string.sudden_mode_desc,
                R.drawable.sudden_screenshot));
        previews.add(new PreviewOption(R.string.challenge_mode_text, R.string.challenge_mode_description,
                R.drawable.challenge_screenshot));
        previews.add(new PreviewOption(R.string.statistics_text, R.string.statistics_description,
                R.drawable.statistics_screenshot));
        PreviewAdapter adapter = new PreviewAdapter(previews);
        viewPager.setAdapter(adapter);
        selectButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            Bundle bundle = new Bundle();
            switch(viewPager.getCurrentItem()) {
                case 0:
                    bundle.putInt("game_mode", 0);
                    navController.navigate(R.id.action_selection_to_game, bundle);
                    break;
                case 1:
                    bundle.putInt("game_mode", 1);
                    navController.navigate(R.id.action_selection_to_game, bundle);
                    break;
                case 2:
                    bundle.putInt("game_mode", 2);
                    navController.navigate(R.id.action_selection_to_game, bundle);
                    break;
                case 3:
                    navController.navigate(R.id.action_selection_to_statistics);
                    break;
            }
        });
    }
}
