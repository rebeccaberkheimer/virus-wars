package edu.commonwealthu.viruswars;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Backend for the game of Virus Wars. Each cell in a square grid contains an X or an O,
 * representing each player's virus colony, or an empty space. Initially, colony O is placed in
 * the bottom left corner, and colony X is placed in the top right corner.
 * For example, the starting configuration on a 3x3 grid, where E represents empty spaces.
 *
 *     E E X
 *     E E E
 *     O E E
 *
 * To start, the O colony will move first and will be given one move to balance out the advantage
 * of starting, then each player will take turns in making three moves in a row.
 * A move can either be the creation of a new virus, by moving to an accessible cell or
 * killing an opponent's virus, by shading over an opponent's symbol in an accessible cell.
 *
 * A cell is accessible if it is horizontal, vertical, or diagonal to a player's existing live virus
 * or if it is connected to one of their live viruses through a chain of the opponent's killed
 * viruses.
 *
 * The first player that is unable to complete all 3 moves of their turn loses.
 *
 * @author Rebecca Berkheimer
 */
public class VirusWars {
    private Occupant[][] grid;
    private Occupant currentPlayer = Occupant.COLONY_O;
    private final Stack<Occupant[][]> gameStates = new Stack<>();
    private final Stack<int[]> moveHistory = new Stack<>();
    private final int mode; // 0 for Standard mode, 1 for Sudden Death, 2 for Challenge
    private boolean firstTurn = true;
    private int moveCount = 0;

    public VirusWars(int size, int mode) {
        grid = new Occupant[size][size];
        this.mode = mode;
        setStartingConfiguration();
    }

    /**
     * Places each player in their correct corners at the start of the game
     */
    private void setStartingConfiguration() {
        for (Occupant[] occupants : grid) {
            // Fill the grid in as all empty initially
            Arrays.fill(occupants, Occupant.EMPTY);
        }
        grid[0][grid.length - 1] = Occupant.COLONY_X;
        grid[grid.length - 1][0] = Occupant.COLONY_O;

        // If in challenge mode, fill 1/6th of the board with obstacles, places them in non edge
        // spaces, I found that to be the most interesting for gameplay
        if (mode == 2) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < (grid.length * grid.length) / 6; i++) {
                int randX = random.nextInt(1, grid.length - 1);
                int randY = random.nextInt(1, grid.length - 1);
                if (grid[randX][randY] == Occupant.EMPTY) {
                    grid[randX][randY] = Occupant.OBSTACLE;
                }
            }
        }
    }

    /**
     * Moves the occupant at (row, col), ignores invalid moves
     */
    public void move(int row, int col) {
        if(isAccessible(row, col)) {
            gameStates.push(copyOfGameState());
            moveHistory.push(new int[]{row, col});

            // Move can create a new virus or kill an opponents
            if (grid[row][col] == Occupant.EMPTY)
                grid[row][col] = currentPlayer;
            else if (grid[row][col] == currentPlayer.opposite()) {
                if (currentPlayer.opposite() == Occupant.COLONY_X) {
                    grid[row][col] = Occupant.KILLED_X;
                } else
                    grid[row][col] = Occupant.KILLED_O;
            }
        }
        moveCount++;

        // For Sudden Death mode, only one turn is allowed
        if (mode == 1) {
            if (moveCount == 1) {
                currentPlayer = currentPlayer.opposite();
                moveCount = 0;
            }
        }
        // After move is made, change the turn based on the count of moves
        // (1 for first turn, 3 for every other turn)
        if (firstTurn) {
            if (moveCount == 1) {
                firstTurn = false; // Set firstTurn to false after the first move
                currentPlayer = currentPlayer.opposite(); // Change turn
                moveCount = 0;
            }
        }
        else {
            if (moveCount == 3) {
                currentPlayer = currentPlayer.opposite();
                moveCount = 0;
            }
        }
    }

    public boolean undo() {
        if (gameStates.isEmpty()) {
            return false; // No moves to undo
        }
        grid = gameStates.pop();
        moveHistory.pop();
        // Undoing a move that is not the last move
        if (moveCount > 0) {
            moveCount--;
        }
        // If the player wants to undo their last move, but control has switched to next player
        // switch to the previous player and set the move count back to the 2nd move
         else {
            moveCount = 2;
            currentPlayer = currentPlayer.opposite();
        }
        return true;
    }

    /**
     * Checks if the position at (row, col) is adjacent (horizontally, vertically, or diagonally) to
     * a live virus of the current player and checks if the position at (row, col) is reachable
     * through a chain of the opponent's killed viruses using breadth first search.
     * Returns true is the position at row, col is accessible to the player through
     * adjacency or an existing chain of killed opponent viruses
     */
    public boolean isAccessible(int row, int col) {
        // Mark already placed cells of the player (live virus or killed opponent) as inaccessible
        if (!checkNotAlreadyPlaced(row, col)) {
            return false;
        }
        if (mode == 2 && grid[row][col] == Occupant.OBSTACLE) {
            return false;
        }
        Queue<int[]> queue = new LinkedList<>();
        // Array to keep track of visited positions in BFS
        boolean[][] marked = new boolean[grid.length][grid.length];
        queue.add(new int[] {row, col});
        marked[row][col] = true;
        while(!queue.isEmpty()) {
            // Remove and search from the position
            int[] position = queue.remove();
            // 8 directions to check, each direction will be in a range of -1 to 1 away from the
            // cell calling the method
            for(int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) {
                        continue; // No directional change
                    }
                    int nextRow = position[0] + i;
                    int nextCol = position[1] + j;

                    // Only check unvisited positions
                    if (inBounds(nextRow, nextCol) && !marked[nextRow][nextCol]) {
                        marked[nextRow][nextCol] = true;
                        Occupant nextPos = grid[nextRow][nextCol];
                        // Reaches a live virus through the chain, making the chain valid
                        if (nextPos == currentPlayer) {
                            return true;
                        }
                        if ((currentPlayer == Occupant.COLONY_O && nextPos == Occupant.KILLED_X) ||
                            (currentPlayer == Occupant.COLONY_X && nextPos == Occupant.KILLED_O)) {
                            // Continue exploring killed virus chain
                            queue.add(new int[]{nextRow, nextCol});
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid.length;
    }

    /**
     * Helper method for checking if the player is trying to place a virus at a position
     * they have already filled (with either their virus or killing the opponent's)
     */
    public boolean checkNotAlreadyPlaced(int row, int col) {
        Occupant occupant = occupantAt(row, col);
        if ((currentPlayer == Occupant.COLONY_O && occupant == Occupant.KILLED_O) ||
                (currentPlayer == Occupant.COLONY_X && occupant == Occupant.KILLED_X)) {
            return false;
        }
        return occupant != currentPlayer &&
                (currentPlayer != Occupant.COLONY_X || occupant != Occupant.KILLED_O) &&
                (currentPlayer != Occupant.COLONY_O || occupant != Occupant.KILLED_X);
    }

    /**
     * Returns the first valid move a player can make
     */
    public boolean canMove() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (isAccessible(i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Occupant getCurrentPlayer() { return currentPlayer; }

    public int size() { return grid.length; }
    public int mode() { return mode; }

    /**
     * Returns a copy of the current game state
     */
    private Occupant[][] copyOfGameState() {
        int n = grid.length;
        Occupant[][] copy = new Occupant[n][n];
        for (int i = 0; i < n; i++) {
            copy[i] = Arrays.copyOf(grid[i], n);
        }
        return copy;
    }

    /**
     * Returns the number of moves made in the game
     */
    public int moves() {
        return gameStates.size();
    }

    /**
     * Returns a sequence of indices at which moves have been made.
     */
    public int[][] moveHistory() {
        int n = moves();
        int[][] a = new int[n][2];
        for (int i = 0; i < a.length; i++) {
            a[i] = moveHistory.get(i);
        }
        return a;
    }

    /**
     * Returns the Occupant at (i, j).
     */
    public Occupant occupantAt(int i, int j) {
        return grid[i][j];
    }
}

