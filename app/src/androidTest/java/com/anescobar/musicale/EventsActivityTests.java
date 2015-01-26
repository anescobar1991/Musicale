package com.anescobar.musicale;

import android.support.test.espresso.assertion.ViewAssertions;
import android.test.ActivityInstrumentationTestCase2;
import com.anescobar.musicale.view.activities.EventsActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


public class EventsActivityTests extends ActivityInstrumentationTestCase2<EventsActivity> {

    public EventsActivityTests() {
        super(EventsActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

//    public void testExploreInMapButtonFunctionality() {
//        onView(withText(R.string.explore_in_map_tab_text))
//                .perform(click());
//
//        onView(withId(R.id.events_map_container))
//                .check(ViewAssertions.matches(isDisplayed()));
//    }
}