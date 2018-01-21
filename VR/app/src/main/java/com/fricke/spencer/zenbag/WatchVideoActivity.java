/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fricke.spencer.zenbag;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.vr.ndk.base.AndroidCompat;
import com.google.vr.ndk.base.GvrLayout;
import com.google.vr.ndk.base.GvrLayout.ExternalSurfaceListener;
import android.util.Log;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Connection;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.OpentokError;
import com.opentok.android.SubscriberKit;

import android.support.annotation.NonNull;
import android.Manifest;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import com.fricke.spencer.message.SignalMessage;
import com.fricke.spencer.message.SignalMessageAdapter;

// delete, for testing
import android.widget.FrameLayout;

public class WatchVideoActivity extends Activity implements Session.SessionListener,
                                                            Session.SignalListener,
                                                            SubscriberKit.SubscriberListener,
                                                            SubscriberKit.VideoListener {

  private static final String TAG = WatchVideoActivity.class.getSimpleName();

  private GvrLayout gvrLayout;
  private VideoSceneRenderer renderer;
  private VideoExoPlayer2 videoPlayer;
  private Settings settings;
//  private boolean hasFirstFrame;

  private static String API_KEY = "46043442";
  private static String SESSION_ID = "2_MX40NjA0MzQ0Mn5-MTUxNjUzMTU4NjU3NH55bDgrWi8vcGxMaHpZeldrMW8rYWJwQ0t-fg";
  private static String TOKEN = "T1==cGFydG5lcl9pZD00NjA0MzQ0MiZzaWc9YjFmOTA0MDJiY2JmNjVjZTFhMDMzMGM1NDE5ZTI0Y2YxNWIwNDE5NjpzZXNzaW9uX2lkPTJfTVg0ME5qQTBNelEwTW41LU1UVXhOalV6TVRVNE5qVTNOSDU1YkRncldpOHZjR3hNYUhwWmVsZHJNVzhyWVdKd1EwdC1mZyZjcmVhdGVfdGltZT0xNTE2NTMxNjA1Jm5vbmNlPTAuMDQzMTI5NzEzMDY1MTkzNTMmcm9sZT1tb2RlcmF0b3ImZXhwaXJlX3RpbWU9MTUxNzEzNjQwNCZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
  private static final String LOG_TAG = WatchVideoActivity.class.getSimpleName();
  private static final int RC_SETTINGS_SCREEN_PERM = 123;
  private static final int RC_VIDEO_APP_PERM = 124;

  private Session mSession;
//  private FrameLayout mPublisherViewContainer;
  private FrameLayout mSubscriberViewContainer;
  private Subscriber mSubscriber;

  private SignalMessageAdapter mMessageHistory;

  // Transform a quad that fills the clip box at Z=0 to a 16:9 screen at Z=-4. Note that the matrix
  // is column-major, so the translation is on the last row rather than the last column in this
  // representation.
  private final float[] videoTransform = {
    1.6f, 0.0f, 0.0f, 0.0f,
    0.0f, 0.9f, 0.0f, 0.0f,
    0.0f, 0.0f, 1.0f, 0.0f,
    0.0f, 0.0f, -4.f, 1.0f
  };


  // Runnable to play/pause the video player. Must be run on the UI thread.
  private final Runnable triggerRunnable =
      new Runnable() {
          @Override
          public void run() {
            if (videoPlayer != null) {
              videoPlayer.togglePause();
            }
          }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    settings = new Settings(this, getIntent().getExtras());

    setImmersiveSticky();
    getWindow()
        .getDecorView()
        .setOnSystemUiVisibilityChangeListener(
            new View.OnSystemUiVisibilityChangeListener() {
              @Override
              public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                  setImmersiveSticky();
                }
              }
            });

    AndroidCompat.setSustainedPerformanceMode(this, true);
    AndroidCompat.setVrModeEnabled(this, true);

    gvrLayout = new GvrLayout(this);

    renderer = new VideoSceneRenderer(this, settings);
    gvrLayout.setPresentationView(renderer.getView());
    gvrLayout.setKeepScreenOn(true);
    renderer.setUpApi( gvrLayout.getGvrApi());

    // Initialize the ExternalSurfaceListener to receive video Surface callbacks.

    ExternalSurfaceListener videoSurfaceListener =
        new ExternalSurfaceListener() {
          @Override
          public void onSurfaceAvailable(Surface surface) {
            // Set the surface for the video player to output video frames to. Video playback
            // is started when the Surface is set. Note that this callback is *asynchronous* with
            // respect to the Surface becoming available, in which case videoPlayer may be null due
            // to the Activity having been stopped.
            Log.i(TAG, "onSurfaceAvailable: " + surface);
            if (videoPlayer != null) {
              videoPlayer.setSurface(surface);
              Log.i(TAG, "Video surface set on player.");
            }
          }

          @Override
          public void onFrameAvailable() {
            // If this is the first frame, and the Activity is still in the foreground, signal to
            // remove the loading splash screen, and draw alpha 0 in the color buffer where the
            // video will be drawn by the GvrApi.
            if (!renderer.firstFrame() && videoPlayer != null) {
              renderer.startedPlaybackTrue();
            }
          }
        };

    // Note that enabling video Surface support will also enable async reprojection.
    boolean isAsyncReprojectionEnabled =
        gvrLayout.enableAsyncReprojectionVideoSurface(
            videoSurfaceListener,
            new Handler(Looper.getMainLooper()),
            /* Whether video playback should use a protected reprojection pipeline. */
            settings.useDrmVideoSample);

    if (!isAsyncReprojectionEnabled) {
      // The device does not support this API, video will not play.
      Log.e(TAG, "UnsupportedException: Async Reprojection with Video is unsupported.");
    } else {
      initVideoPlayer();

      // The default value puts the viewport behind the eye, so it's invisible. Set the transform
      // now to ensure the video is visible when rendering starts.
      renderer.setVideoTransform(videoTransform);
      // The ExternalSurface buffer the GvrApi should reference when drawing the video buffer. This
      // must be called after enabling the Async Reprojection video surface.
      renderer.setVideoSurfaceId(gvrLayout.getAsyncReprojectionVideoSurfaceId());

      // Simulate cardboard trigger to play/pause video playback.
      gvrLayout.enableCardboardTriggerEmulation(triggerRunnable);

    }

    // Set the renderer and start the app's GL thread.
    renderer.setViewRenderer();

    setContentView(gvrLayout);

    requestPermissions();
  }

  @Override
  public void onSignalReceived(Session session, String type, String data, Connection connection) {
    boolean remote = !connection.equals(mSession.getConnection());
    Log.i(TAG, "SIGNAL DATA TEXT");
    Log.i(TAG, type);

    if (type.equals("1".toString())) {
      Log.i(TAG, "A");
    }

    if (type.compareTo("1") == 0) {
      Log.i(TAG, "B");
    }
    if (type != null && type.equals("1")) {
      Log.i(TAG, "V");
    } else if (type != null && type.equals("0")) {
      Log.i(TAG, "CLEAR");
    }

    SignalMessage message = new SignalMessage(data, remote);
    mMessageHistory.add(message);

    if (data.length() > 10) {
      Log.i(TAG, data.substring(0, 10));
    } else {
      Log.i(TAG, "NOPPOO");
    }

  }

  private void initVideoPlayer() {
    videoPlayer = new VideoExoPlayer2(getApplication(), settings);
    Uri streamUri;
    String drmVideoId = null;

    if (settings.useDrmVideoSample) {
      // Protected video, requires a secure path for playback.
      Log.i(TAG, "Using DRM-protected video sample.");
      streamUri = Uri.parse("https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd");
      drmVideoId = "0894c7c8719b28a0";
    } else {
      // Unprotected video, does not require a secure path for playback.
      Log.i(TAG, "Using cleartext video sample.");
      streamUri = Uri.parse("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd");
    }

    try {
      videoPlayer.initPlayer(streamUri, drmVideoId);
      renderer.setVideoPlayer(videoPlayer);
    } catch (UnsupportedDrmException e) {
      Log.e(TAG, "Error initializing video player", e);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (videoPlayer == null) {
      initVideoPlayer();
//
//      videoPlayer.releasePlayer();
//      videoPlayer = null;
//      videoPlayer.pause();
//      super.onPause();
    }

    renderer.startedPlaybackFalse();
    // Resume the surfaceView and gvrLayout here. This will start the render thread and trigger a
    // new async reprojection video Surface to become available.
    renderer.onResume();
    gvrLayout.onResume();

    onPause();
    // Refresh the viewer profile in case the viewer params were changed.

    // Runnable to refresh the viewer profile when gvrLayout is resumed.
    // This is done on the GL thread because refreshViewerProfile isn't thread-safe.
    new Runnable() {
      @Override
      public void run() {
        gvrLayout.getGvrApi().refreshViewerProfile();
      }
    };

  }

  @Override
  protected void onResume() {
    super.onResume();
//    if (videoPlayer.isPaused()) {
//      videoPlayer.play();
//    }
  }

  @Override
  protected void onPause() {
    // Pause video playback. The video Surface may be detached before onStop() is called,
    // but will remain valid if the activity life-cycle returns to onResume(). Pause the
    // player to avoid dropping video frames.
    videoPlayer.pause();
    super.onPause();
  }

  @Override
  protected void onStop() {
    if (videoPlayer != null) {
      renderer.setVideoPlayer(null);
      videoPlayer.releasePlayer();
      videoPlayer = null;
    }
    // Pause the gvrLayout and surfaceView here. The video Surface is guaranteed to be detached and
    // not available after gvrLayout.onPause(). We pause from onStop() to avoid needing to wait
    // for an available video Surface following brief onPause()/onResume() events. Wait for the
    // new onSurfaceAvailable() callback with a valid Surface before resuming the video player.
    gvrLayout.onPause();
    renderer.onPause();
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    gvrLayout.shutdown();
    settings = null;
    super.onDestroy();
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    // Avoid accidental volume key presses while the phone is in the VR headset.
    if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
        || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
      return true;
    }
    return super.dispatchKeyEvent(event);
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      setImmersiveSticky();
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    gvrLayout.onBackPressed();
  }

  private void setImmersiveSticky() {
    getWindow()
        .getDecorView()
        .setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  }

  /**
   * Returns true if video is currently playing. This method should only be called in the UI thread.
   */
  public boolean isVideoPlaying() {
    return (renderer.firstFrame() && !isVideoPaused());
  }

  /**
   * Returns true if video is currently paused. This method should only be called in the UI thread.
   */
  public boolean isVideoPaused() {
    return (videoPlayer != null) && (videoPlayer.isPaused());
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  @AfterPermissionGranted(RC_VIDEO_APP_PERM)
  private void requestPermissions() {
    String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
    if (EasyPermissions.hasPermissions(this, perms)) {
      // initialize view objects from your layout
//      mSubscriberViewContainer = new FrameLayout(this);
      mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);

      // initialize and connect to the session
      mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
      mSession.setSessionListener(this);
      mSession.setSignalListener(this);
      mSession.connect(TOKEN);


    } else {
      EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
    }
  }

  @Override
  public void onConnected(Session session) {
    Log.i(LOG_TAG, "Session Connected");
  }

  @Override
  public void onDisconnected(Session session) {
    Log.i(LOG_TAG, "Session Disconnected");
  }

  @Override
  public void onConnected(SubscriberKit subscriberKit) {
    Log.i(LOG_TAG, "Subscriber Connected");

    if (mSubscriber == null) {
      Log.i(LOG_TAG, "Subscriber IS NULL");
    }
    if (mSubscriber.getView() == null) {
      Log.i(LOG_TAG, "GetView IS NULL");
    }
//      mSubscriber = new Subscriber(this, stream);
//      mSubscriber.setSubscriberListener(this);
//      mSubscriber.setRenderer(renderer);
//      renderer.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
//              BaseVideoRenderer.STYLE_VIDEO_FILL);
//      mSession.subscribe(mSubscriber);
//           mSubscriberViewContainer.addView(mSubscriber.getView());
//    }

  }
  @Override
  public void onVideoDataReceived(SubscriberKit var1) {
    renderer.setStreaming(true);
    Log.i(LOG_TAG, "Video DATA RECEIVED onVideoDataReceived");
  }
  @Override
  public void onVideoDisabled(SubscriberKit var1, String var2) {
    Log.i(LOG_TAG, "Video DATA RECEIVED onVideoDisabled");}
  @Override
  public void onVideoEnabled(SubscriberKit var1, String var2) {
    Log.i(LOG_TAG, "Video DATA RECEIVED onVideoEnabled");}
  @Override
  public void onVideoDisableWarning(SubscriberKit var1) {
    renderer.setStreaming(false);
    Log.i(LOG_TAG, "Video DATA RECEIVED onVideoDisableWarning");
  }

  @Override
  public void onVideoDisableWarningLifted(SubscriberKit var1) {
    Log.i(LOG_TAG, "Video DATA RECEIVED onVideoDisableWarningLifted");
  }




  @Override
  public void onDisconnected(SubscriberKit subscriberKit) {
    renderer.setStreaming(false);
    Log.i(LOG_TAG, "Subscriber Disconnected");
  }

  @Override
  public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
    Log.i(LOG_TAG, "S onError");
  }

  @Override
  public void onStreamReceived(Session session, Stream stream) {
    Log.i(LOG_TAG, "Stream Received");


    if (mSubscriber == null) {
      mSubscriber = new Subscriber.Builder(this, stream).build();
      mSubscriber.setSubscriberListener(this);
      mSubscriber.setRenderer(renderer);
      mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
              BaseVideoRenderer.STYLE_VIDEO_FILL);
      mSession.subscribe(mSubscriber);
    }
  }

  @Override
  public void onStreamDropped(Session session, Stream stream) {
    Log.i(LOG_TAG, "Stream Dropped");

    if (mSubscriber != null) {
      mSubscriber = null;
//      mSubscriberViewContainer.removeAllViews();
    }
  }

  @Override
  public void onError(Session session, OpentokError opentokError) {
    Log.e(LOG_TAG, "Session error: " + opentokError.getMessage());
  }


}
