package edu.commonwealthu.viruswars;

/**
 * Used for storing the data returned by the query for a player's win count. Room requires mapping
 * query results to an object when multiple columns are returned. From the query, the winner and
 * winCount are mapped to the respective fields.
 *
 * @author Rebecca Berkheimer
 */
public class WinStats {
    private final Occupant winner;
    private final int winCount;

    public WinStats(Occupant winner, int winCount) {
        this.winner = winner;
        this.winCount = winCount;
    }

    public Occupant getWinner() {
        return winner;
    }
    public int getWinCount() {
        return winCount;
    }
}
