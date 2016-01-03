package org.karlwelzel.vertretungsplan.material;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TabHost;
import android.widget.TextView;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Karl on 07.10.2015.
 */
public class SubstituteScheduleTabHost extends TabHost {

    private SubstituteScheduleListViewAdapter adapter1;
    private SubstituteScheduleListViewAdapter adapter2;

    private SubstituteSchedule substituteSchedule;

    public SubstituteScheduleTabHost(Context context) {
        super(context);
    }

    public SubstituteScheduleTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(SubstituteScheduleListViewAdapter adapter1, SubstituteScheduleListViewAdapter adapter2) {
        setup();
        this.adapter1 = adapter1;
        this.adapter2 = adapter2;
        addTab(newTabSpec("first").setIndicator(getResources().getString(R.string.today)).setContent(R.id.swipeRefresh1)); //setup tab 1
        addTab(newTabSpec("second").setIndicator(getResources().getString(R.string.tomorrow)).setContent(R.id.swipeRefresh2)); //setup tab 2
        setChildTabEnabled(0, false);
        setChildTabEnabled(1, false);
    }

    public void setChildTabEnabled(int index, boolean enabled) {
        getTabWidget().getChildTabViewAt(index).setAlpha((float) (enabled ? 1 : .5)); //enable/disable tab index
        getTabWidget().getChildTabViewAt(index).setEnabled(enabled);
    }

    public void setSubstituteSchedule(SubstituteSchedule substituteSchedule) throws JSONException {
        this.substituteSchedule = substituteSchedule;
        long nowMilliseconds = (new Date()).getTime();

        adapter1.setSubstituteScheduleDay(null);
        adapter2.setSubstituteScheduleDay(null);

        for (int i = substituteSchedule.size() - 1; i >= 0; i--) {
            Date date = this.substituteSchedule.getDate(i);
            long timeDifference = nowMilliseconds - date.getTime();
            if (timeDifference < 0) { //future
                TextView title = (TextView) getTabWidget().getChildAt(1).findViewById(android.R.id.title);
                if (-24 * 60 * 60 * 1000 < timeDifference) { //tomorrow
                    title.setText(getResources().getString(R.string.tomorrow));
                } else if (-7 * 24 * 60 * 60 * 1000 < timeDifference) { //this week
                    title.setText((new SimpleDateFormat("EEEE", Locale.GERMAN)).format(date));
                } else {
                    title.setText((new SimpleDateFormat("EEEE '('d.M.')'", Locale.GERMAN)).format(date));
                }
                adapter2.setSubstituteScheduleDay(this.substituteSchedule.getDay(i));
                setChildTabEnabled(1, true);
                setCurrentTab(1);
            } else if (0 < timeDifference && timeDifference < 24 * 60 * 60 * 1000) { //if first day is today
                adapter1.setSubstituteScheduleDay(this.substituteSchedule.getDay(i));
                setChildTabEnabled(0, true);
                if (!adapter2.substituteScheduleDaySet() || adapter2.isEmpty() || timeDifference < 16 * 60 * 60 * 1000) {
                    setCurrentTab(0);
                }
            }
        }
    }
}
