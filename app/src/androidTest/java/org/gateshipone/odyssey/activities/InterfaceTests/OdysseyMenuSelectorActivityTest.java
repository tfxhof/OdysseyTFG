/*
 * Copyright (C) 2023 Team Gateship-One
 * (Hendrik Borghorst & Frederik Luetkes)
 *
 * The AUTHORS.md file contains a detailed contributors list:
 * <https://github.com/gateship-one/odyssey/blob/master/AUTHORS.md>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.gateshipone.odyssey.activities.InterfaceTests;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import org.gateshipone.odyssey.R;

import org.gateshipone.odyssey.activities.OdysseyMenuSelectorActivity;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class OdysseyMenuSelectorActivityTest {

    @Rule
    public ActivityScenarioRule<OdysseyMenuSelectorActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(OdysseyMenuSelectorActivity.class);

    @Test
    public void odysseyMenuSelectorActivityTestArtists() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.button_artists), withText("Artists"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatButton.perform(click());

        //Check the view pager is visible
        onView(ViewMatchers.withId(R.id.my_music_viewpager)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //Check position
        onView(withId(R.id.my_music_viewpager)).check(isAtPosition(0));
    }

    @Test
    public void odysseyMenuSelectorActivityTestAlbums() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.button_albums), withText("Albums"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton.perform(click());
        //Check the view pager is visible
        onView(ViewMatchers.withId(R.id.my_music_viewpager)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //Check position
        onView(withId(R.id.my_music_viewpager)).check(isAtPosition(1));
    }

    @Test
    public void odysseyMenuSelectorActivityTestPlaylists() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.button_playlists), withText("My Playlists"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton.perform(click());
        //Check the view pager is visible
        onView(ViewMatchers.withId(R.id.my_music_viewpager)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //Check position
        onView(withId(R.id.my_music_viewpager)).check(isAtPosition(3));
    }

    @Test
    public void odysseyMenuSelectorActivityTestMyMusic() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.button_songs), withText("My songs"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatButton.perform(click());
        //Check the view pager is visible
        onView(ViewMatchers.withId(R.id.my_music_viewpager)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //Check position
        onView(withId(R.id.my_music_viewpager)).check(isAtPosition(2));
    }

    @Test
    public void odysseyMenuSelectorActivityTestBack() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.button_artists), withText("Artists"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatButton.perform(click());
        pressBack();
        onView(withId(R.id.button_artists)).check(matches(isDisplayed()));
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
    private static ViewAssertion isAtPosition(final int position) {
        return new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                if (noViewFoundException != null) {
                    throw noViewFoundException;
                }

                if (!(view instanceof ViewPager)) {
                    throw new IllegalArgumentException("The view is not a ViewPager");
                }

                ViewPager viewPager = (ViewPager) view;
                int currentItem = viewPager.getCurrentItem();
                assertThat("Expected current item: " + position, currentItem, is(position));
            }
        };
    }


}
