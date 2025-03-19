package edu.commonwealthu.viruswars;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object used for querying the CompletedGame table.
 *
 * @author Rebecca Berkheimer
 */
@Dao
public interface StatsDAO {
    @Insert
    long insertCompleteGame(CompletedGame completedGame);

    @Query("SELECT COUNT(*) FROM CompletedGame")
    int getTotalGamesPlayed();

    @Query("SELECT AVG(numOfMoves) FROM CompletedGame")
    int getAverageMoves();

    @Query("SELECT winner, COUNT(*) as winCount FROM CompletedGame GROUP BY winner")
    List<WinStats> getPlayerWins();
}
