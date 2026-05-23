package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.core.app.ActivityScenario
import org.robolectric.annotation.Config
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MainActivityTest {
    @Test
    fun testActivityLaunch() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assert(activity != null)
            }
        }
    }

    @Test
    fun testDatabaseAndDaos() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val count = db.templateDao().getTemplateCount()
            assert(count == 0)
        } finally {
            db.close()
        }
    }
}
