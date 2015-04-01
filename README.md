# Android Audio

High-level library for efficient playback of sounds and music on Android

## Installation

 * Include one of the [JARs](JARs) in your `libs` folder
 * or
 * Copy the Java package to your project's source folder
 * or
 * Create a new library project from this repository and reference it in your project

## Usage

### Playing music and one-off sound files

```
MusicManager.getInstance().play(MyActivity.this, R.raw.my_sound);
```

### Playing sounds (frequently and fast)

```
class MyActivity extends Activity {

	private SoundManager mSoundManager;

	@Override
	protected void onResume() {
		super.onResume();

		int maxSimultaneousStreams = 3;
		mSoundManager = new SoundManager(this, maxSimultaneousStreams);
		mSoundManager.start();
		mSoundManager.load(R.raw.my_sound_1);
		mSoundManager.load(R.raw.my_sound_2);
		mSoundManager.load(R.raw.my_sound_3);
	}

	private void playSomeSound() {
		if (mSoundManager != null) {
			mSoundManager.play(R.raw.my_sound_2);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mSoundManager != null) {
			mSoundManager.cancel();
			mSoundManager = null;
		}
	}

}
```

## Dependencies

 * Android 2.2+

## Contributing

All contributions are welcome! If you wish to contribute, please create an issue first so that your feature, problem or question can be discussed.

## License

```
Copyright 2015 delight.im <info@delight.im>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
