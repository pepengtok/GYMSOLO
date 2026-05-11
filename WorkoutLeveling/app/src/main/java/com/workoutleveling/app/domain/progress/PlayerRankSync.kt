package com.workoutleveling.app.domain.progress

import com.workoutleveling.app.data.local.db.PlayerStateDao
import com.workoutleveling.app.data.local.db.WorkoutSessionDao

object PlayerRankSync {
    suspend fun refreshRankFromHistory(
        sessionDao: WorkoutSessionDao,
        playerStateDao: PlayerStateDao,
    ) {
        val now = System.currentTimeMillis()
        val windowStart = now - 28L * 24 * 60 * 60 * 1000
        val recent = sessionDao.listStartedBetween(windowStart, now)
        val row = playerStateDao.getRow() ?: return
        val rank = ProgressRules.rankFromRollingWindow(now, recent)
        if (rank != row.rankCode) {
            playerStateDao.upsert(row.copy(rankCode = rank))
        }
    }
}
