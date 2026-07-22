package view;

import model.Position;

public class SelectionState {

    private Position selected;

    public Position get() {
        return selected;
    }

    public boolean isSelected() {
        return selected != null;
    }

    public boolean isSameAsSelected(Position position) {
        return selected != null && selected.equals(position);
    }

    public void select(Position position) {
        this.selected = position;
    }

    public void clear() {
        this.selected = null;
    }
}
