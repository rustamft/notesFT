package com.rustamft.notesft;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rustamft.notesft.activities.MainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class HundredNotesTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void hundredNotesTest() {
        ViewInteraction materialButton = onView(
                allOf(withId(R.id.button_permission), withText("Choose directory"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_host),
                                        0),
                                0),
                        isDisplayed()));
        materialButton.perform(click());

        // Cycle
        for (int i = 0; i < 100; i++) {
            ViewInteraction floatingActionButton = onView(
                    allOf(withId(R.id.fab_add), withContentDescription("Add"),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.nav_host),
                                            0),
                                    1),
                            isDisplayed()));
            floatingActionButton.perform(click());

            ViewInteraction appCompatEditText = onView(
                    allOf(withId(R.id.edittext_create),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.custom),
                                            0),
                                    0),
                            isDisplayed()));
            appCompatEditText.perform(replaceText("z" + i), closeSoftKeyboard());

            ViewInteraction materialButton2 = onView(
                    allOf(withId(android.R.id.button1), withText("Apply"),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.buttonPanel),
                                            0),
                                    3)));
            materialButton2.perform(scrollTo(), click());

            ViewInteraction appCompatEditText2 = onView(
                    allOf(withId(R.id.edittext_note),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.nav_host),
                                            0),
                                    0),
                            isDisplayed()));
            appCompatEditText2.perform(replaceText("z" + i), closeSoftKeyboard());

            ViewInteraction appCompatImageButton = onView(
                    allOf(withContentDescription("Navigate up"),
                            childAtPosition(
                                    allOf(withId(R.id.action_bar),
                                            childAtPosition(
                                                    withId(R.id.action_bar_container),
                                                    0)),
                                    2),
                            isDisplayed()));
            appCompatImageButton.perform(click());

            ViewInteraction materialButton3 = onView(
                    allOf(withId(android.R.id.button1), withText("Save"),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.buttonPanel),
                                            0),
                                    3)));
            materialButton3.perform(scrollTo(), click());
        }
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
