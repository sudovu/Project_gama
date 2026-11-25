package com.example

import com.example.core.util.CuePointManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CuePointManagerTest {

    @Test
    fun insertQueryAndDeleteCuePoints() {
        val context = RuntimeEnvironment.getApplication()
        val trackId = "cue_mgr_test_track"

        val c1 = CuePointManager.addCuePoint(context, trackId, 32000L, "Drop")
        val c2 = CuePointManager.addCuePoint(context, trackId, 12000L, "Intro")

        val queried = CuePointManager.getCuePoints(context, trackId)
        assertEquals(2, queried.size)
        // Sorted by positionMs ascending
        assertEquals(c2.id, queried[0].id)
        assertEquals("Intro", queried[0].label)
        assertEquals(c1.id, queried[1].id)
        assertEquals("Drop", queried[1].label)

        CuePointManager.deleteCuePoint(context, trackId, c2.id)
        val afterDelete = CuePointManager.getCuePoints(context, trackId)
        assertEquals(1, afterDelete.size)
        assertEquals(c1.id, afterDelete[0].id)

        CuePointManager.deleteCuePoint(context, trackId, c1.id)
        assertTrue(CuePointManager.getCuePoints(context, trackId).isEmpty())
    }
}
