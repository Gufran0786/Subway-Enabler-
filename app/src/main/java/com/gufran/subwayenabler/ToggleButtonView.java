package com.gufran.subwayenabler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ToggleButtonView {

    public interface OnToggle {
        void onToggle(boolean newState);
    }

    /**
     * Ek row banao — icon + title + subtitle + ON/OFF toggle.
     * Parent LinearLayout me add karo aur view return karo.
     */
    public static View build(Context ctx,
                             LinearLayout parent,
                             int iconRes,
                             String title,
                             String subtitle,
                             boolean initialState,
                             OnToggle callback) {

        View row = LayoutInflater.from(ctx)
            .inflate(R.layout.row_item, parent, false);

        ImageView icon = row.findViewById(R.id.icon);
        TextView tvTitle = row.findViewById(R.id.title);
        TextView tvSub = row.findViewById(R.id.subtitle);
        TextView toggle = row.findViewById(R.id.toggle);

        icon.setImageResource(iconRes);
        tvTitle.setText(title);
        tvSub.setText(subtitle);

        final boolean[] state = { initialState };
        renderToggle(toggle, state[0]);

        // Toggle button pe tap
        toggle.setOnClickListener(v -> {
            state[0] = !state[0];
            renderToggle(toggle, state[0]);
            if (callback != null) callback.onToggle(state[0]);
        });

        // Poori row pe tap bhi toggle trigger kare
        row.setOnClickListener(v -> toggle.performClick());

        parent.addView(row);
        return row;
    }

    private static void renderToggle(TextView tv, boolean on) {
        if (on) {
            tv.setText("ON");
            tv.setBackgroundResource(R.drawable.btn_toggle_on);
        } else {
            tv.setText("OFF");
            tv.setBackgroundResource(R.drawable.btn_toggle_off);
        }
    }
}
