package com.trigonated.ctwtopheadlines

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.trigonated.ctwtopheadlines.misc.MockNewsApi
import com.trigonated.ctwtopheadlines.misc.hasViewWithTextAtPosition
import com.trigonated.ctwtopheadlines.misc.waitFor
import com.trigonated.ctwtopheadlines.model.mock.MockObjects
import com.trigonated.ctwtopheadlines.model.objects.Article
import com.trigonated.ctwtopheadlines.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class TopHeadlinesFragmentTest {

    @get:Rule(order = 0)
    var hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityRule: ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        MockNewsApi.setupStubs()
    }

    /**
     * Test a normal loading interaction.
     * The UI should present a list of articles.
     */
    @Test
    fun loadData() {
        Espresso.onView(withId(R.id.list)).perform(waitFor(isDisplayed(), 5000))
        val firstItem: Article = MockObjects.createMockPaginatedArticleListFromLastApiMock().items.first()
        Espresso.onView(withId(R.id.list)).check(hasViewWithTextAtPosition(0, firstItem.title ?: ""))
    }
}