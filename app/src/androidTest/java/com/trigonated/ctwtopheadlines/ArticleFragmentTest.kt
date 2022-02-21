package com.trigonated.ctwtopheadlines

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.trigonated.ctwtopheadlines.misc.MockNewsApi
import com.trigonated.ctwtopheadlines.misc.waitFor
import com.trigonated.ctwtopheadlines.model.mock.MockObjects
import com.trigonated.ctwtopheadlines.model.objects.Article
import com.trigonated.ctwtopheadlines.ui.MainActivity
import com.trigonated.ctwtopheadlines.ui.top_headlines.TopHeadlinesArticleListAdapter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class ArticleFragmentTest {

    @get:Rule(order = 0)
    var hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityRule: ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        MockNewsApi.setupStubs()
        // Load the list and click on an item
        Espresso.onView(withId(R.id.list)).perform(waitFor(isDisplayed(), 5000))
        Espresso.onView(withId(R.id.list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<TopHeadlinesArticleListAdapter.ArticleViewHolder>(
                0,
                click()
            )
        )
    }

    /**
     * Test a normal loading interaction.
     * The article's details should be visible on-screen.
     */
    @Test
    fun loadData() {
        Thread.sleep(500)
        val expectedItem: Article = MockObjects.createMockPaginatedArticleListFromLastApiMock().items.first()
        Espresso.onView(withId(R.id.title_text)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.title_text)).check(matches(withText(expectedItem.title)))
        Espresso.onView(withId(R.id.description_text)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.description_text)).check(matches(withText(expectedItem.description)))
        Espresso.onView(withId(R.id.content_text)).check(matches(isDisplayed()))
        Espresso.onView(withId(R.id.content_text)).check(matches(withText(expectedItem.content)))
        if (expectedItem.isContentTruncated) {
            Espresso.onView(withId(R.id.read_full_article_button)).check(matches(isDisplayed()))
        } else {
            Espresso.onView(withId(R.id.read_full_article_button)).check(matches(not(isDisplayed())))
        }
    }
}