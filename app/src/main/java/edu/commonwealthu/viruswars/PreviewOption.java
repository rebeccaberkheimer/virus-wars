package edu.commonwealthu.viruswars;

/**
 * Represents a PreviewOption used in the ViewPager2.
 *
 * @author Rebecca Berkheimer
 */
public class PreviewOption {
    int modeID;
    int descriptionID;
    int imageID;
    public PreviewOption(int m, int desc, int id) {
        modeID = m;
        descriptionID = desc;
        imageID = id;
    }

    public int getMode() {
        return modeID;
    }

    public int getDescription() {
        return descriptionID;
    }

    public int getImageID() {
        return imageID;
    }
}
