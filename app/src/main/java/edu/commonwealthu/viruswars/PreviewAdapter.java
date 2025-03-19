package edu.commonwealthu.viruswars;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.commonwealthu.viruswars.R;

/**
 * Adapter class for the ViewPager2 in the SelectionFragment. Supplies the preview data
 * and defines its layout. Binds each PreviewOption element to its mode, image, and description.
 *
 * @author Rebecca Berkheimer
 */
public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.PreviewViewHolder> {

    private final ArrayList<PreviewOption> previewOptions;
    public PreviewAdapter(ArrayList<PreviewOption> previews) {
        previewOptions = previews;
    }

    @NonNull
    @Override
    public PreviewAdapter.PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_option,
                parent, false);
        return new PreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewAdapter.PreviewViewHolder holder, int position) {
        PreviewOption preview = previewOptions.get(position);
        holder.modeText.setText(preview.getMode());
        holder.imagePreview.setImageResource(preview.getImageID());
        holder.descText.setText(preview.getDescription());
    }

    @Override
    public int getItemCount() {
        return previewOptions.size();
    }

    public static class PreviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView modeText;
        private final ImageView imagePreview;
        private final TextView descText;
        public PreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            modeText = itemView.findViewById(R.id.mode_preview_text);
            imagePreview = itemView.findViewById(R.id.image_preview);
            descText = itemView.findViewById(R.id.mode_description_text);
        }
    }
}
