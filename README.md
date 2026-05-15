# AR Face

Android app for real-time AR face filters. Uses Google MediaPipe FaceLandmarker
to track 478 facial landmarks at camera framerate and overlays PNG-based masks
(cat ears, sunglasses + hat, alien, makeup, etc.) on top of a CameraX preview.

Optionally, new filters can be generated on the fly from a text prompt via the
Gemini API.

## Features

- Live front-camera preview with MediaPipe face tracking (478 landmarks)
- Built-in mask gallery: cat ears, sunglasses + hat, alien, makeup
- Blendshape-driven effects (smile → hearts, jaw open → fire, blink → flash)
- Photo capture + share intent
- AI filter generation from a text prompt (optional, requires Gemini key)

## Requirements

- Android Studio Ladybug or newer
- Android SDK 36 (compileSdk / targetSdk = 36, minSdk = 26)
- JDK 17
- A physical Android device with a front camera (the emulator's virtual camera
  works but face tracking quality is limited)

## Installation

```bash
git clone https://github.com/ATelbay/ar_face_app.git
cd ar_face_app
```

Open the project in Android Studio and let Gradle sync. Then either:

- Click **Run** in Android Studio, or
- Build and install from the command line:

```bash
./gradlew :app:installDebug
```

The app requests camera permission on first launch.

## Optional: AI filter generation

To enable the "New filter" button (generate a filter from a text prompt),
add your Gemini API key to `local.properties` in the project root:

```properties
gemini.api.key=YOUR_KEY_HERE
```

Without a key, the app still runs — it falls back to a stub that returns one
of the built-in masks. Get a key at https://aistudio.google.com/apikey.

`local.properties` is gitignored; the key never lands in version control.

## Usage

1. Launch the app and grant camera permission.
2. Point the front camera at your face — face tracking starts automatically.
3. Tap a mask thumbnail in the bottom gallery to apply it.
4. Try the blendshape-driven effects:
   - Smile → hearts particles
   - Open your jaw → fire
   - Blink → flash
5. Tap the shutter button to capture a photo. The result is saved and a share
   sheet opens.
6. Tap **New filter** (➕) to describe a custom filter ("space cat", "cyberpunk
   visor", …) and have it generated.

## Project structure

```
app/src/main/java/com/arystan/arface/
  ai/         Gemini client + stubs for AI filter generation
  camera/     CameraX wrapper and Compose preview
  capture/    Photo capture and share intent
  effects/    Blendshape analyzer + particle overlays
  face/       MediaPipe FaceLandmarker integration
  mask/       Mask descriptor model, repository, renderer
  ui/         ARFaceScreen, ViewModel, gallery, prompt dialog
app/src/main/assets/
  face_landmarker.task   MediaPipe model (mmap'd from APK, do not compress)
  masks/                 Built-in mask layers + thumbnails + JSON descriptors
```

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- CameraX (core / camera2 / lifecycle / view)
- MediaPipe Tasks Vision — FaceLandmarker
- Coil for image loading, OkHttp for the Gemini call
- kotlinx.coroutines, kotlinx.serialization
