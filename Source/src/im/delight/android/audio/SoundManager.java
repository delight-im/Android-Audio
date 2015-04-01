package im.delight.android.audio;

/**
 * Copyright 2015 www.delight.im <info@delight.im>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import android.media.AudioManager;
import java.util.Map;
import android.media.SoundPool;

/** Plays sounds (frequently and fast) while managing the resources efficiently */
@SuppressLint("UseSparseArrays")
public class SoundManager extends Thread {

	private static class SoundManagerTask {

		private static final int ACTION_LOAD = 1;
		private static final int ACTION_PLAY = 2;
		private static final int ACTION_UNLOAD = 3;
		private static final int ACTION_CANCEL = 4;
		private final int mSoundResourceId;
		private final float mVolume;
		private final int mRepetitions;
		private final int mAction;

		private SoundManagerTask(final int soundResourceId, final float volume, final int repetitions, final int action) {
			mSoundResourceId = soundResourceId;
			mVolume = volume;
			mRepetitions = repetitions;
			mAction = action;
		}

		public static SoundManagerTask load(final int soundResourceId) {
			return new SoundManagerTask(soundResourceId, 0, 0, ACTION_LOAD);
		}

		public static SoundManagerTask play(final int soundResourceId, final float volume, final int repetitions) {
			return new SoundManagerTask(soundResourceId, volume, repetitions, ACTION_PLAY);
		}

		public static SoundManagerTask unload(final int soundResourceId) {
			return new SoundManagerTask(soundResourceId, 0, 0, ACTION_UNLOAD);
		}

		public static SoundManagerTask cancel() {
			return new SoundManagerTask(0, 0, 0, ACTION_CANCEL);
		}

		public int getSoundResourceId() {
			return mSoundResourceId;
		}

		public float getVolume() {
			return mVolume;
		}

		public int getRepetitions() {
			return mRepetitions;
		}

		public boolean isLoad() {
			return mAction == ACTION_LOAD;
		}

		public boolean isPlay() {
			return mAction == ACTION_PLAY;
		}

		public boolean isUnload() {
			return mAction == ACTION_UNLOAD;
		}

		public boolean isCancel() {
			return mAction == ACTION_CANCEL;
		}

	}

	private final SoundPool mSoundPool;
	private final Context mContext;
	private final Map<Integer, Integer> mSounds;
	private final BlockingQueue<SoundManagerTask> mTasks = new LinkedBlockingQueue<SoundManagerTask>();
	private volatile boolean mCancelled;

	@SuppressWarnings("deprecation")
	public SoundManager(final Context context, final int maxSimultaneousStreams) {
		mSoundPool = new SoundPool(maxSimultaneousStreams, AudioManager.STREAM_MUSIC, 0);
		mContext = context.getApplicationContext();
		mSounds = new HashMap<Integer, Integer>();
	}

	public void load(final int soundResourceId) {
		try {
			mTasks.put(SoundManagerTask.load(soundResourceId));
		}
		catch (InterruptedException e) { }
	}

	public void play(final int soundResourceId) {
		play(soundResourceId, 1.0f);
	}

	public void play(final int soundResourceId, final float volume) {
		play(soundResourceId, volume, 0);
	}

	public void play(final int soundResourceId, final float volume, final int repetitions) {
		if (!isAlive()) {
			return;
		}

		try {
			mTasks.put(SoundManagerTask.play(soundResourceId, volume, repetitions));
		}
		catch (InterruptedException e) { }
	}

	public void unload(final int soundResourceId) {
		try {
			mTasks.put(SoundManagerTask.unload(soundResourceId));
		}
		catch (InterruptedException e) { }
	}

	public void cancel() {
		try {
			mTasks.put(SoundManagerTask.cancel());
		}
		catch (InterruptedException e) { }
	}

	@Override
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

		try {
			SoundManagerTask task;
			while (!mCancelled) {
				task = mTasks.take();
				if (task.isCancel()) {
					mCancelled = true;
					break;
				}
				else {
					final Integer currentMapping;
					synchronized (mSounds) {
						currentMapping = mSounds.get(task.getSoundResourceId());
					}

					if (task.isLoad()) {
						if (currentMapping == null) {
							final int newMapping = mSoundPool.load(mContext, task.getSoundResourceId(), 1);

							synchronized (mSounds) {
								mSounds.put(task.getSoundResourceId(), newMapping);
							}
						}
					}
					else if (task.isPlay()) {
						if (currentMapping != null) {
							mSoundPool.play(currentMapping.intValue(), task.getVolume(), task.getVolume(), 0, task.getRepetitions(), 1.0f);
						}
					}
					else if (task.isUnload()) {
						if (currentMapping != null) {
							mSoundPool.unload(currentMapping.intValue());

							synchronized (mSounds) {
								mSounds.remove(task.getSoundResourceId());
							}
						}
					}
				}
			}
		}
		catch (InterruptedException e) { }

		if (mSounds != null) {
			synchronized (mSounds) {
				mSounds.clear();
			}
		}

		if (mSoundPool != null) {
			mSoundPool.release();
		}
	}

}
