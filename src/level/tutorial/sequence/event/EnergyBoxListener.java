package level.tutorial.sequence.event;

import level.Level;
import unit.action.Action;

public class EnergyBoxListener {
    public static TutorialEventListener openIncomeBox() {
        return e -> e instanceof IncomeBoxEvent;
    }

    public static class IncomeBoxEvent extends TutorialEvent {

        public IncomeBoxEvent(Level l) {
            super(l);
        }
    }
}
