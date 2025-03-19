package edu.commonwealthu.viruswars;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing the CompletedGame table for the StatsDatabase
 *
 * @author Rebecca Berkheimer
 */
@Entity
public class CompletedGame {

    @PrimaryKey(autoGenerate = true)
    public int gameID;
    public int mode;
    public int size;
    public int numOfMoves;
    public Occupant winner;

    public CompletedGame(int mode, int size, int numOfMoves, Occupant winner) {
        this.mode = mode;
        this.size = size;
        this.numOfMoves = numOfMoves;
        this.winner = winner;
    }

}
