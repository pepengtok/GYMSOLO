package com.workoutleveling.app.ui.session

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.workoutleveling.app.R

class CoachSoundPlayer(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val readyId = soundPool.load(context, R.raw.coach_ready_beep, 1)
    private val setFinishId = soundPool.load(context, R.raw.coach_set_finish, 1)
    private val restEndId = soundPool.load(context, R.raw.coach_rest_end, 1)
    private val tapId = soundPool.load(context, R.raw.coach_tap, 1)

    fun playReadyTick() = soundPool.play(readyId, 0.9f, 0.9f, 1, 0, 1f)
    fun playSetFinish() = soundPool.play(setFinishId, 0.95f, 0.95f, 1, 0, 1f)
    fun playRestEnd() = soundPool.play(restEndId, 1f, 1f, 1, 0, 1f)
    fun playTap() = soundPool.play(tapId, 0.6f, 0.6f, 1, 0, 1f)

    fun release() {
        soundPool.release()
    }
}
