package cz.steuer.gtdapp.enums;

import cz.steuer.gtdapp.R;

/**
 * Created by honza on 12/18/13.
 */
public enum TaskCategory {
    INBOX(R.string.category_inbox, R.color.category_inbox),
    NEXT(R.string.category_next, R.color.category_next),
    SCHEDULED(R.string.category_scheduled, R.color.category_scheduled),
    WAITING(R.string.category_waiting, R.color.category_waiting),
    SUSPENDED(R.string.category_suspended, R.color.category_suspended);

    private int title;
    private int color;
    private TaskCategory(int title, int color) {
        this.title = title;
        this.color = color;
    }

    public int getTitle() {
        return title;
    }

    public int getColor() {
        return color;
    }
}
