package edu.commonwealthu.viruswars;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Room database used for storing statistics about completed games on the user's device
 *
 * @author Rebecca Berkheimer
 */
@Database(entities = {CompletedGame.class}, version = 2, exportSchema = false)
public abstract class StatsDatabase extends RoomDatabase {
    public abstract StatsDAO getStatsDAO();
}
