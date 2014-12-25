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
public class ConverterTest extends ActivityInstrumentationTestCase2<Converter> {

    public ConverterTest() {
        super(Converter.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testOpenConverter() {


        onView(withId(R.id.converter_ipaddress)).perform(typeText("192.168.1.13"));

        onView(withId(R.id.convertdec)).perform(click());

    }


}