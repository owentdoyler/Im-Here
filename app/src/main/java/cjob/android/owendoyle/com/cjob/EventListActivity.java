/*
* A simple activity that hosts the EventListFragment
* */
package cjob.android.owendoyle.com.cjob;

import android.support.v4.app.Fragment;

import cjob.android.owendoyle.com.cjob.database.EventsDatabaseHelper;

/**
 * Created by Comhghall on 04/11/2015.
 */
public class EventListActivity extends SingleFragmentActivity {
    @Override

    //creates the event list fragment
    protected Fragment createFragment() {
        return new EventListFragment();
    }
}
