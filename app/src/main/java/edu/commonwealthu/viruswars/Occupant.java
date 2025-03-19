package edu.commonwealthu.viruswars;

/**
 * Contents of the grid cells in Virus Wars
 *
 * @author Rebecca Berkheimer
 */
public enum Occupant {
    EMPTY,
    COLONY_O,
    COLONY_X,
    KILLED_O,
    KILLED_X,
    OBSTACLE;

    public Occupant opposite() {
        switch(this) {
            case COLONY_O:
                return COLONY_X;
            case COLONY_X:
                return COLONY_O;
            default:
                return EMPTY;
        }
    }
}
