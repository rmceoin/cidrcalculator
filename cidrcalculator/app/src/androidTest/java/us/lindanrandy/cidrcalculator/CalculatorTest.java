package us.lindanrandy.cidrcalculator;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

@LargeTest
public class CalculatorTest extends ActivityInstrumentationTestCase2<CIDRCalculator> {

    public CalculatorTest() {
        super(CIDRCalculator.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testOpenConverter() {

        onView(withId(R.id.reset)).perform(click());

        onView(withId(R.id.ipaddress)).perform(typeText("192.168.1.13"));

        onView(withId(R.id.calculate)).perform(click());

        // Open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Click the item.
        onView(withText("Converter"))
                .perform(click());

        onView(withId(R.id.converter_ipaddress))
                .check(matches(withText("192.168.1.13")));

    }

    public void testCalculateWithBitlength() {

        onView(withId(R.id.reset)).perform(click());

        onView(withId(R.id.ipaddress)).perform(typeText("192.168.0.10"));

        onView(withId(R.id.bitlength)).perform(click());

        onData(allOf(is(instanceOf(String.class)), is("/30")))
                .perform(click());

        onView(withId(R.id.address_range))
                .check(matches(withText("192.168.0.8 - 192.168.0.11")));
        onView(withId(R.id.maximum_addresses))
                .check(matches(withText("2")));
        onView(withId(R.id.wildcard))
                .check(matches(withText("0.0.0.3")));

        onView(withId(R.id.reset)).perform(click());

        onView(withId(R.id.address_range))
                .check(matches(withText("")));
        onView(withId(R.id.maximum_addresses))
                .check(matches(withText("")));
        onView(withId(R.id.wildcard))
                .check(matches(withText("")));

        onView(withId(R.id.ipaddress)).perform(typeText("192.168.0.12"));

        onView(withId(R.id.bitlength)).perform(click());

        onData(allOf(is(instanceOf(String.class)), is("/2")))
                .perform(click());

        onView(withId(R.id.address_range))
                .check(matches(withText("192.0.0.0 - 255.255.255.255")));
        onView(withId(R.id.maximum_addresses))
                .check(matches(withText("1073741822")));
        onView(withId(R.id.wildcard))
                .check(matches(withText("63.255.255.255")));

    }

    /**
     * Creates a matcher against the text stored in R.id.item_content. This text is roughly
     * "item: $row_number".
     */
/*
    public static Matcher<Object> withItemContent(String expectedText) {
        // use preconditions to fail fast when a test is creating an invalid matcher.
        checkNotNull(expectedText);
        return withItemContent(equalTo(expectedText));
    }

    public static Matcher<Object> withItemContent(final Matcher<String> itemTextMatcher) {
        checkNotNull(itemTextMatcher);
        return new BoundedMatcher<Object, Map>(Map.class) {
            @Override
            public boolean matchesSafely(Map map) {
                return hasEntry(equalTo("STR"), itemTextMatcher).matches(map);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with item content: ");
                itemTextMatcher.describeTo(description);
            }
        };
    }
*/
}