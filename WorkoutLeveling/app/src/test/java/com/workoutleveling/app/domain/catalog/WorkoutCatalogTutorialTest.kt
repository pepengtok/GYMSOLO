package com.workoutleveling.app.domain.catalog

import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutCatalogTutorialTest {
    @Test
    fun tutorialImageCandidates_useExpectedSlugPattern() {
        val candidates = WorkoutCatalog.tutorialImageCandidatesForName("Chest press (mesin HG60)")
        assertTrue(candidates.first().contains("image/tutorial/chest_press_mesin_hg60"))
    }

    @Test
    fun tutorialVideoCandidates_includeGifFallback() {
        val candidates = WorkoutCatalog.tutorialVideoCandidatesForName("Dead bug")
        assertTrue(candidates.any { it.endsWith(".gif") })
    }
}
