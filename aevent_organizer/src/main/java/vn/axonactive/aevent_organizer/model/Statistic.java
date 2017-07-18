package vn.axonactive.aevent_organizer.model;

/**
 * Created by ltphuc on 3/13/2017.
 */

public class Statistic {

    private int total;
    private int checked;

    public Statistic(int checked, int total) {
        this.total = total;
        this.checked = checked;
    }

    public int getTotal() {
        return total;
    }

    public int getChecked() {
        return checked;
    }

    public int getUnchecked() {
        return total - checked;
    }
}
