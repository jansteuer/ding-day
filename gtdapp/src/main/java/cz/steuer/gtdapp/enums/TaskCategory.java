package cz.steuer.gtdapp.enums;

import cz.steuer.gtdapp.R;

/**
 * Created by honza on 12/18/13.
 */
public enum TaskCategory {
    INBOX(R.string.category_inbox, R.color.category_inbox, R.drawable.ic_action_download),
    NEXT(R.string.category_next, R.color.category_next, R.drawable.ic_action_send_now),
    SCHEDULED(R.string.category_scheduled, R.color.category_scheduled, R.drawable.ic_action_go_to_today),
    WAITING(R.string.category_waiting, R.color.category_waiting, R.drawable.ic_action_alarms),
    SUSPENDED(R.string.category_suspended, R.color.category_suspended, R.drawable.ic_action_pause);

    private int title;
    private int color;
    private int icon;
    private TaskCategory(int title, int color, int icon) {
        this.title = title;
        this.color = color;
        this.icon = icon;
    }

    public int getTitle() {
        return title;
    }

    public int getColor() { return color;
    }
    public int getIcon() {
        return icon;
    }


}
