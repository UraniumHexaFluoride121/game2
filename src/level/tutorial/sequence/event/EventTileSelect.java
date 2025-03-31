package level.tutorial.sequence.event;

import level.Level;

import java.awt.*;

public class EventTileSelect extends TutorialEvent {
    public final Point tile;

    public EventTileSelect(Level l, Point tile) {
        super(l);
        this.tile = tile;
    }
}
