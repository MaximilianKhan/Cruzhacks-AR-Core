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

package com.google.vr.sdk.samples.treasurehunt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;

import com.google.vr.sdk.samples.message.SignalMessage;
import com.google.vr.sdk.samples.message.SignalMessageAdapter;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

/**
 * A Google VR sample application.
 *
 * <p>The TreasureHunt scene consists of a planar ground grid and a floating
 * "treasure" cube. When the user looks at the cube, the cube will turn gold.
 * While gold, the user can activate the Cardboard trigger, either directly
 * using the touch trigger on their Cardboard viewer, or using the Daydream
 * controller-based trigger emulation. Activating the trigger will in turn
 * randomly reposition the cube.
 */
public class TreasureHuntActivity extends GvrActivity implements GvrView.StereoRenderer,
                                                                WebServiceCoordinator.Listener,
                                                                Session.SessionListener,
                                                                Session.SignalListener,
                                                                SubscriberKit.SubscriberListener,
                                                                Subscriber.VideoListener{
  private static final String LOG_TAG = TreasureHuntActivity.class.getSimpleName();
  public static final String SIGNAL_TYPE = "text-signal";

  private WebServiceCoordinator mWebServiceCoordinator;

  private Session mSession;
  private Subscriber mSubscriber;
  private FrameLayout mSubscriberViewContainer;
  private SignalMessageAdapter mMessageHistory;

//  private EditText mMessageEditTextView;
//  private ListView mMessageHistoryListView;
  //// ^^^^ Tokbox /////////

  protected float[] modelCube;
  protected float[] modelPosition;

  private static final String TAG = "TreasureHuntActivity";

  private static final float Z_NEAR = 0.1f;
  private static final float Z_FAR = 100.0f;

  private static final float CAMERA_Z = 0.01f;
  private static final float TIME_DELTA = 0.3f;

  private static final float YAW_LIMIT = 0.12f;
  private static final float PITCH_LIMIT = 0.12f;

  private static final int COORDS_PER_VERTEX = 3;

  // We keep the light always position just above the user.
  private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] {0.0f, 2.0f, 0.0f, 1.0f};

  // Convenience vector for extracting the position from a matrix via multiplication.
  private static final float[] POS_MATRIX_MULTIPLY_VEC = {0, 0, 0, 1.0f};

  private static final float MIN_MODEL_DISTANCE = 3.0f;
  private static final float MAX_MODEL_DISTANCE = 7.0f;

  private static final String OBJECT_SOUND_FILE = "cube_sound.wav";
  private static final String SUCCESS_SOUND_FILE = "success.wav";

  private final float[] lightPosInEyeSpace = new float[4];

  private FloatBuffer floorVertices;
  private FloatBuffer floorColors;
  private FloatBuffer floorNormals;

  private FloatBuffer cubeVertices;
  private FloatBuffer cubeColors;
  private FloatBuffer cubeFoundColors;
  private FloatBuffer cubeNormals;
  private FloatBuffer drawVertices;

  private int cubeProgram;
  private int floorProgram;

  private int cubePositionParam;
  private int cubeNormalParam;
  private int cubeColorParam;
  private int cubeModelParam;
  private int cubeModelViewParam;
  private int cubeModelViewProjectionParam;
  private int cubeLightPosParam;

  private int floorPositionParam;
  private int floorNormalParam;
  private int floorColorParam;
  private int floorModelParam;
  private int floorModelViewParam;
  private int floorModelViewProjectionParam;
  private int floorLightPosParam;

  private float[] camera;
  private float[] view;
  private float[] headView;
  private float[] modelViewProjection;
  private float[] modelView;
  private float[] modelFloor;

  private float[] tempPosition;
  private float[] headRotation;

  private float objectDistance = MAX_MODEL_DISTANCE / 2.0f;
  private float floorDepth = 20f;

  private Vibrator vibrator;

  private GvrAudioEngine gvrAudioEngine;
  private volatile int sourceId = GvrAudioEngine.INVALID_ID;
  private volatile int successSourceId = GvrAudioEngine.INVALID_ID;

  private ControllerManager controllerManager;
  private Controller controller;

  private static float xHead = 0;
  private static float zHead = 0;
  private static float xTouch;
  private static float yTouch;
  private static boolean aButton;
  private static boolean bButton;
  private static boolean yButton;
  private static boolean zButton;

  /**
   * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
   *
   * @param type The type of shader we will be creating.
   * @param resId The resource ID of the raw text file about to be turned into a shader.
   * @return The shader object handler.
   */
  private int loadGLShader(int type, int resId) {
    String code = readRawTextFile(resId);
    int shader = GLES20.glCreateShader(type);
    GLES20.glShaderSource(shader, code);
    GLES20.glCompileShader(shader);

    // Get the compilation status.
    final int[] compileStatus = new int[1];
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

    // If the compilation failed, delete the shader.
    if (compileStatus[0] == 0) {
      Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
      GLES20.glDeleteShader(shader);
      shader = 0;
    }

    if (shader == 0) {
      throw new RuntimeException("Error creating shader.");
    }

    return shader;
  }

  /**
   * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
   *
   * @param label Label to report in case of error.
   */
  private static void checkGLError(String label) {
    int error;
    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
      Log.e(TAG, label + ": glError " + error);
      throw new RuntimeException(label + ": glError " + error);
    }
  }

  private Handler uiHandler = new Handler();

  /**
   * Sets the view to our GvrView and initializes the transformation matrices we will use
   * to render our scene.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initializeGvrView();

//    mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);

    // Start the ControllerManager and acquire a Controller object which represents a single
    // physical controller. Bind our listener to the ControllerManager and Controller.
    EventListener listener = new EventListener();
    controllerManager = new ControllerManager(this, listener);
//    apiStatusView.setText("Binding to VR Service");
    controller = controllerManager.getController();
    controller.setEventListener(listener);


    modelCube = new float[16];
    camera = new float[16];
    view = new float[16];
    modelViewProjection = new float[16];
    modelView = new float[16];
    modelFloor = new float[16];
    tempPosition = new float[4];
    // Model first appears directly in front of user.
    modelPosition = new float[] {0.0f, 0.0f, -MAX_MODEL_DISTANCE / 2.0f};
    headRotation = new float[4];
    headView = new float[16];
    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    // Initialize 3D audio engine.
    gvrAudioEngine = new GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);




    //// TOKBOX ///
    // inflate views
//    mMessageEditTextView = (EditText)findViewById(R.id.message_edit_text);
//    mMessageHistoryListView = (ListView)findViewById(R.id.message_history_list_view);

    // Attach data source to message history
    mMessageHistory = new SignalMessageAdapter(this);

    // initialize the session after validating configs

    // if there is no server URL assiged
    if (OpenTokConfig.CHAT_SERVER_URL == null) {
      // use hard coded session info
      if (OpenTokConfig.areHardCodedConfigsValid()) {
        initializeSession(OpenTokConfig.API_KEY, OpenTokConfig.SESSION_ID, OpenTokConfig.TOKEN);
      } else {
        showConfigError("Configuration Error", OpenTokConfig.hardCodedConfigErrorMessage);
      }
    } else {
      // otherwise initialize WebServiceCoordinator and kick off request for session data
      // session initialization occurs once data is returned, in onSessionConnectionDataReady
      if (OpenTokConfig.isWebServerConfigUrlValid()) {
        mWebServiceCoordinator = new WebServiceCoordinator(this, this);
        mWebServiceCoordinator.fetchSessionConnectionData(OpenTokConfig.SESSION_INFO_ENDPOINT);
      } else {
        showConfigError("Configuration Error", OpenTokConfig.webServerConfigErrorMessage);
      }
    }
  }
  @Override
  protected void onStart() {
    super.onStart();
    Log.i("ABC", "START");
    controllerManager.start();
  }

  @Override
  protected void onStop() {
    controllerManager.stop();
    super.onStop();
  }

  private void initializeSession(String apiKey, String sessionId, String token) {

    Log.d(LOG_TAG, "Initializing Session");

    mSession = new Session.Builder(this, apiKey, sessionId).build();
    mSession.setSessionListener(this);
    mSession.setSignalListener(this);
    mSession.connect(token);
  }

  private void sendMessage() {

    Log.d(LOG_TAG, "Send Message");

//    SignalMessage signal = new SignalMessage(mMessageEditTextView.getText().toString());
//    mSession.sendSignal(SIGNAL_TYPE, signal.getMessageText());

//    mMessageEditTextView.setText("");

  }

  private void showMessage(String messageData, boolean remote) {

    Log.d(LOG_TAG, "Show Message");

    SignalMessage message = new SignalMessage(messageData, remote);
    mMessageHistory.add(message);
  }

  private void logOpenTokError(OpentokError opentokError) {

    Log.e(LOG_TAG, "Error Domain: " + opentokError.getErrorDomain().name());
    Log.e(LOG_TAG, "Error Code: " + opentokError.getErrorCode().name());
  }

  public void initializeGvrView() {
    setContentView(R.layout.common_ui);

    GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
    gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

    gvrView.setRenderer(this);
    gvrView.setTransitionViewEnabled(false);

    // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of supporting
    // Daydream controller input for basic interactions using the existing Cardboard trigger API.
//    gvrView.enableCardboardTriggerEmulation();

//    if (gvrView.setAsyncReprojectionEnabled(true)) {
//      // Async reprojection decouples the app framerate from the display framerate,
//      // allowing immersive interaction even at the throttled clockrates set by
//      // sustained performance mode.
//      AndroidCompat.setSustainedPerformanceMode(this, true);
//    }

    setGvrView(gvrView);
  }

  @Override
  public void onPause() {
    gvrAudioEngine.pause();
    super.onPause();

    if (mSession != null) {
      mSession.onPause();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    gvrAudioEngine.resume();

    if (mSession != null) {
      mSession.onResume();
    }
  }

  /* Session Listener methods */

  @Override
  public void onConnected(Session session) {
    Log.i(LOG_TAG, "Session Connected");
//    mMessageEditTextView.setEnabled(true);
  }

  @Override
  public void onDisconnected(Session session) {
    Log.i(LOG_TAG, "Session Disconnected");
  }

  @Override
  public void onError(Session session, OpentokError opentokError) {
    logOpenTokError(opentokError);
  }


  /* Signal Listener methods */

  @Override
  public void onSignalReceived(Session session, String type, String data, Connection connection) {

    if (type.compareTo("0") == 0) {
      Log.i("ABC", "CLEAR: " + type);
      clearCubes();
    } else {
      String[] token = data.split(",");
      Log.i("ABC", "Data Length: " + data.length());

      float[] cubeData = new float[token.length];
      for (int i = 0; i < cubeData.length; i++) {
        cubeData[i] = Float.parseFloat(token[i]);
      }
      addCubes(cubeData);
    }
    boolean remote = !connection.equals(mSession.getConnection());
    if (type != null && type.equals(SIGNAL_TYPE)) {
      showMessage(data, remote);
    }
  }

  private void addCubes(float[] data) {
    int cubesAdded = data.length / 4;

    if (CUBE_COUNT + cubesAdded > MAX_CUBES) {
      Log.i("ABC", "OUUT OF MEMORYT");
      return; //throw new Error("Out of memory");
    }

    cubeVertices.position(108 * CUBE_COUNT);
    cubeColors.position(144 * CUBE_COUNT);
    cubeNormals.position(108 * CUBE_COUNT);
    for (int i = 0; i < cubesAdded; i += 4) {
      cubeVertices.put(WorldLayoutData.getCubeCoords(
              -data[i+2] * OFFSET,
              data[i+1] * OFFSET,
              (-data[i] * OFFSET) + OFFSET_Z,
              data[i+3] * OFFSET
      ));
      cubeColors.put(WorldLayoutData.CUBE_FOUND_COLORS);
      cubeNormals.put(WorldLayoutData.CUBE_NORMALS);
    }
    cubeVertices.position(0);
    cubeColors.position(0);
    cubeNormals.position(0);

    CUBE_COUNT += cubesAdded;
  }

  private static final float OFFSET = 10.0f;
  private static final float OFFSET_Z = 10.0f;

  private void clearCubes() {

    CUBE_COUNT = 1; // just main cube

    cubeVertices.position(0);
    cubeColors.position(0);
    cubeNormals.position(0);
  }

    /* Web Service Coordinator delegate methods */

  @Override
  public void onSessionConnectionDataReady(String apiKey, String sessionId, String token) {

    Log.d(LOG_TAG, "ApiKey: "+apiKey + " SessionId: "+ sessionId + " Token: "+token);

    initializeSession(apiKey, sessionId, token);
  }

  @Override
  public void onWebServiceCoordinatorError(Exception error) {
    showConfigError("Web Service error", error.getMessage());
  }

    /* alert dialogue for errors */

  private void showConfigError(String alertTitle, final String errorMessage) {

    Log.e(LOG_TAG, "Error " + alertTitle + ": " + errorMessage);
    new AlertDialog.Builder(this)
            .setTitle(alertTitle)
            .setMessage(errorMessage)
            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                TreasureHuntActivity.this.finish();
              }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
  }

  @Override
  public void onRendererShutdown() {
    Log.i(TAG, "onRendererShutdown");
  }

  @Override
  public void onSurfaceChanged(int width, int height) {
    Log.i(TAG, "onSurfaceChanged");
  }

  private static final int MAX_CUBES = 1000;
  private static int CUBE_COUNT = 1; // main cube is given no matter what
  /**
   * Creates the buffers we use to store information about the 3D world.
   *
   * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
   * Hence we use ByteBuffers.
   *
   * @param config The EGL configuration used when creating the surface.
   */
  @Override
  public void onSurfaceCreated(EGLConfig config) {
    Log.i(TAG, "onSurfaceCreated");
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

    ByteBuffer bbVertices = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COORDS.length * 4 * MAX_CUBES);
    bbVertices.order(ByteOrder.nativeOrder());
    cubeVertices = bbVertices.asFloatBuffer();
    cubeVertices.put(WorldLayoutData.CUBE_COORDS);
    cubeVertices.position(0); // 108


    ByteBuffer bbColors = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COLORS.length * 4  * MAX_CUBES);
    bbColors.order(ByteOrder.nativeOrder());
    cubeColors = bbColors.asFloatBuffer();
    cubeColors.put(WorldLayoutData.CUBE_COLORS);
    cubeColors.position(0); // 144

    ByteBuffer bbFoundColors =
        ByteBuffer.allocateDirect(WorldLayoutData.CUBE_FOUND_COLORS.length * 4);
    bbFoundColors.order(ByteOrder.nativeOrder());
    cubeFoundColors = bbFoundColors.asFloatBuffer();
    cubeFoundColors.put(WorldLayoutData.CUBE_FOUND_COLORS);
    cubeFoundColors.position(0);

    ByteBuffer bbNormals = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_NORMALS.length * 4  * MAX_CUBES);
    bbNormals.order(ByteOrder.nativeOrder());
    cubeNormals = bbNormals.asFloatBuffer();
    cubeNormals.put(WorldLayoutData.CUBE_NORMALS);
    cubeNormals.position(0); // 108

    // make a floor
    ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COORDS.length * 4);
    bbFloorVertices.order(ByteOrder.nativeOrder());
    floorVertices = bbFloorVertices.asFloatBuffer();
    floorVertices.put(WorldLayoutData.FLOOR_COORDS);
    floorVertices.position(0);

    ByteBuffer bbFloorNormals = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_NORMALS.length * 4);
    bbFloorNormals.order(ByteOrder.nativeOrder());
    floorNormals = bbFloorNormals.asFloatBuffer();
    floorNormals.put(WorldLayoutData.FLOOR_NORMALS);
    floorNormals.position(0);

    ByteBuffer bbFloorColors = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COLORS.length * 4);
    bbFloorColors.order(ByteOrder.nativeOrder());
    floorColors = bbFloorColors.asFloatBuffer();
    floorColors.put(WorldLayoutData.FLOOR_COLORS);
    floorColors.position(0);

    int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
    int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
    int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);

    cubeProgram = GLES20.glCreateProgram();
    GLES20.glAttachShader(cubeProgram, vertexShader);
    GLES20.glAttachShader(cubeProgram, passthroughShader);
    GLES20.glLinkProgram(cubeProgram);
    GLES20.glUseProgram(cubeProgram);

    checkGLError("Cube program");

    cubePositionParam = GLES20.glGetAttribLocation(cubeProgram, "a_Position");
    cubeNormalParam = GLES20.glGetAttribLocation(cubeProgram, "a_Normal");
    cubeColorParam = GLES20.glGetAttribLocation(cubeProgram, "a_Color");

    cubeModelParam = GLES20.glGetUniformLocation(cubeProgram, "u_Model");
    cubeModelViewParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVMatrix");
    cubeModelViewProjectionParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVP");
    cubeLightPosParam = GLES20.glGetUniformLocation(cubeProgram, "u_LightPos");

    checkGLError("Cube program params");

    floorProgram = GLES20.glCreateProgram();
    GLES20.glAttachShader(floorProgram, vertexShader);
    GLES20.glAttachShader(floorProgram, gridShader);
    GLES20.glLinkProgram(floorProgram);
    GLES20.glUseProgram(floorProgram);

    checkGLError("Floor program");

    floorModelParam = GLES20.glGetUniformLocation(floorProgram, "u_Model");
    floorModelViewParam = GLES20.glGetUniformLocation(floorProgram, "u_MVMatrix");
    floorModelViewProjectionParam = GLES20.glGetUniformLocation(floorProgram, "u_MVP");
    floorLightPosParam = GLES20.glGetUniformLocation(floorProgram, "u_LightPos");

    floorPositionParam = GLES20.glGetAttribLocation(floorProgram, "a_Position");
    floorNormalParam = GLES20.glGetAttribLocation(floorProgram, "a_Normal");
    floorColorParam = GLES20.glGetAttribLocation(floorProgram, "a_Color");

    checkGLError("Floor program params");

    Matrix.setIdentityM(modelFloor, 0);
    Matrix.translateM(modelFloor, 0, 0, -floorDepth, 0); // Floor appears below user.

    // Avoid any delays during start-up due to decoding of sound files.
//    new Thread(
//            new Runnable() {
//              @Override
//              public void run() {
//                // Start spatial audio playback of OBJECT_SOUND_FILE at the model position. The
//                // returned sourceId handle is stored and allows for repositioning the sound object
//                // whenever the cube position changes.
//                gvrAudioEngine.preloadSoundFile(OBJECT_SOUND_FILE);
//                sourceId = gvrAudioEngine.createSoundObject(OBJECT_SOUND_FILE);
//                gvrAudioEngine.setSoundObjectPosition(
//                    sourceId, modelPosition[0], modelPosition[1], modelPosition[2]);
//                gvrAudioEngine.playSound(sourceId, true /* looped playback */);
//                // Preload an unspatialized sound to be played on a successful trigger on the cube.
//                gvrAudioEngine.preloadSoundFile(SUCCESS_SOUND_FILE);
//              }
//            })
//        .start();

    updateModelPosition();

    checkGLError("onSurfaceCreated");
  }

  /**
   * Updates the cube model position.
   */
  protected void updateModelPosition() {
    Matrix.setIdentityM(modelCube, 0);
    Matrix.translateM(modelCube, 0, modelPosition[0], modelPosition[1], modelPosition[2]);
    Log.v("ABC", modelPosition[0] + "  " +  modelPosition[1] + "   "  + modelPosition[2]);

    // Update the sound location to match it with the new cube position.
//    if (sourceId != GvrAudioEngine.INVALID_ID) {
//      gvrAudioEngine.setSoundObjectPosition(
//          sourceId, modelPosition[0], modelPosition[1], modelPosition[2]);
//    }
    checkGLError("updateCubePosition");
  }

  /**
   * Converts a raw text file into a string.
   *
   * @param resId The resource ID of the raw text file about to be turned into a shader.
   * @return The context of the text file, or null in case of error.
   */
  private String readRawTextFile(int resId) {
    InputStream inputStream = getResources().openRawResource(resId);
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      reader.close();
      return sb.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static final float MOVE_FACTOR = 0.3f;
  private static final float MAX_MOVE = 30.0f;
  private  static boolean test = false;
  /**
   * Prepares OpenGL ES before we draw a frame.
   *
   * @param headTransform The head transformation in the new frame.
   */
  @Override
  public void onNewFrame(HeadTransform headTransform) {
    setCubeRotation();

    // ADD SOMEF CUBVEFSEFV
    if (aButton) {
      if (test) {return;}
      test = true;

      float[] data = { 1.0f, 1.0f, 1.0f,
              1.0f, 2.0f, 1.0f,
              1.0f, 3.0f, 1.0f,
              1.0f, 4.0f, 1.0f,
              1.0f, 5.0f, 1.0f,
              2.0f, 1.0f, 1.0f,
              4.0f, 5.0f, 1.0f,
              6.0f, 5.0f, 1.0f,
              8.0f, 5.0f, 1.0f,
              1.0f, 5.0f, 4.0f,
              1.0f, 5.0f, 8.0f,
              1.0f, 5.0f, 12.0f,
              1.0f, 5.0f, 16.0f,
      };

    }

    if (yButton) {
      Log.i("ABC", "cube count: " + CUBE_COUNT);

      xHead = 0;
      zHead = 0;
    }

    // 0.5 represents a full move factor
    if (xTouch < 0.50) {
      xHead += ( (0.5 - xTouch)/0.5 )  * MOVE_FACTOR; // 0 == + 1
    } else {
      // When it is greater than 0.50
      xHead -= ( (xTouch - 0.5)/0.5 ) * MOVE_FACTOR; // 1 == 0;
    }

    if (yTouch < 0.50) {
      zHead += ( (0.5 - yTouch)/0.5 ) * MOVE_FACTOR;
    } else {
      // When it is greater than 0.50
      zHead -= ( (yTouch - 0.5)/0.5 ) * MOVE_FACTOR;
    }

    if (xHead > MAX_MOVE ) { xHead = MAX_MOVE; }
    if (xHead < -MAX_MOVE) { xHead = -MAX_MOVE; }
    if (zHead > MAX_MOVE ) { zHead = MAX_MOVE; }
    if (zHead < -MAX_MOVE) { zHead = -MAX_MOVE; }

//    Matrix.translateM(modelCube, 0, xHead, 0.0f, zHead);

    // Build the camera matrix and apply it to the ModelView.
    Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

    Matrix.translateM(camera, 0, xHead, 0.0f, zHead);


    headTransform.getHeadView(headView, 0);

    // Update the 3d audio engine with the most recent head rotation.
    headTransform.getQuaternion(headRotation, 0);
//    gvrAudioEngine.setHeadRotation(
//        headRotation[0], headRotation[1], headRotation[2], headRotation[3]);
//    // Regular update call to GVR audio engine.
//    gvrAudioEngine.update();

    checkGLError("onReadyToDraw");
  }

  private static final float ROTATE_FACTOR = 0.1f;
  protected void setCubeRotation() {
//    Matrix.rotateM(modelCube, 0, TIME_DELTA, 0.0f, ROTATE_FACTOR, 0.0f);
  }

  /**
   * Draws a frame for an eye.
   *
   * @param eye The eye to render. Includes all required transformations.
   */
  @Override
  public void onDrawEye(Eye eye) {
    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    checkGLError("colorParam");

    // Apply the eye transformation to the camera.
    Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

    // Set the position of the light
    Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

    // Build the ModelView and ModelViewProjection matrices
    // for calculating cube position and light.
    float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
    Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
    Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
    drawCube();

    // Set modelView for the floor, so we draw floor in the correct location
    Matrix.multiplyMM(modelView, 0, view, 0, modelFloor, 0);
    Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
    drawFloor();
  }

  @Override
  public void onFinishFrame(Viewport viewport) {}

  /**
   * Draw the cube.
   *
   * <p>We've set all of our transformation matrices. Now we simply pass them into the shader.
   */
  public void drawCube() {
    GLES20.glUseProgram(cubeProgram);

    GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);

    // Set the Model in the shader, used to calculate lighting
    GLES20.glUniformMatrix4fv(cubeModelParam, 1, false, modelCube, 0);

    // Set the ModelView in the shader, used to calculate lighting
    GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, modelView, 0);

    // Set the position of the cube
    GLES20.glVertexAttribPointer(
        cubePositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, cubeVertices);

    // Set the ModelViewProjection matrix in the shader.
    GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam, 1, false, modelViewProjection, 0);

    // Set the normal positions of the cube, again for shading
    GLES20.glVertexAttribPointer(cubeNormalParam, 3, GLES20.GL_FLOAT, false, 0, cubeNormals);
    GLES20.glVertexAttribPointer(cubeColorParam, 4, GLES20.GL_FLOAT, false, 0, cubeColors);
//        isLookingAtObject() ? cubeFoundColors : cubeColors);

    // Enable vertex arrays
    GLES20.glEnableVertexAttribArray(cubePositionParam);
    GLES20.glEnableVertexAttribArray(cubeNormalParam);
    GLES20.glEnableVertexAttribArray(cubeColorParam);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36 * CUBE_COUNT);

    // Disable vertex arrays
    GLES20.glDisableVertexAttribArray(cubePositionParam);
    GLES20.glDisableVertexAttribArray(cubeNormalParam);
    GLES20.glDisableVertexAttribArray(cubeColorParam);
    
    checkGLError("Drawing cube");
  }

  /**
   * Draw the floor.
   *
   * <p>This feeds in data for the floor into the shader. Note that this doesn't feed in data about
   * position of the light, so if we rewrite our code to draw the floor first, the lighting might
   * look strange.
   */
  public void drawFloor() {
    GLES20.glUseProgram(floorProgram);

    // Set ModelView, MVP, position, normals, and color.
    GLES20.glUniform3fv(floorLightPosParam, 1, lightPosInEyeSpace, 0);
    GLES20.glUniformMatrix4fv(floorModelParam, 1, false, modelFloor, 0);
    GLES20.glUniformMatrix4fv(floorModelViewParam, 1, false, modelView, 0);
    GLES20.glUniformMatrix4fv(floorModelViewProjectionParam, 1, false, modelViewProjection, 0);
    GLES20.glVertexAttribPointer(
        floorPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, floorVertices);
    GLES20.glVertexAttribPointer(floorNormalParam, 3, GLES20.GL_FLOAT, false, 0, floorNormals);
    GLES20.glVertexAttribPointer(floorColorParam, 4, GLES20.GL_FLOAT, false, 0, floorColors);

    GLES20.glEnableVertexAttribArray(floorPositionParam);
    GLES20.glEnableVertexAttribArray(floorNormalParam);
    GLES20.glEnableVertexAttribArray(floorColorParam);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 24);

    GLES20.glDisableVertexAttribArray(floorPositionParam);
    GLES20.glDisableVertexAttribArray(floorNormalParam);
    GLES20.glDisableVertexAttribArray(floorColorParam);

    checkGLError("drawing floor");
  }

  /**
   * Called when the Cardboard trigger is pulled.
   */
//  @Override
//  public void onCardboardTrigger() {
//    Log.i(TAG, "onCardboardTrigger");
//
//    if (isLookingAtObject()) {
//      //successSourceId = gvrAudioEngine.createStereoSound(SUCCESS_SOUND_FILE);
//      //gvrAudioEngine.playSound(successSourceId, false /* looping disabled */);
//      //hideObject();
//    }
//
//    // Always give user feedback.
//    vibrator.vibrate(50);
//  }

  /**
   * Find a new random position for the object.
   *
   * <p>We'll rotate it around the Y-axis so it's out of sight, and then up or down by a little bit.
   */
  protected void hideObject() {
    float[] rotationMatrix = new float[16];
    float[] posVec = new float[4];

    // First rotate in XZ plane, between 90 and 270 deg away, and scale so that we vary
    // the object's distance from the user.
    float angleXZ = (float) Math.random() * 180 + 90;
    Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
    float oldObjectDistance = objectDistance;
    objectDistance =
        (float) Math.random() * (MAX_MODEL_DISTANCE - MIN_MODEL_DISTANCE) + MIN_MODEL_DISTANCE;
    float objectScalingFactor = objectDistance / oldObjectDistance;
    Matrix.scaleM(rotationMatrix, 0, objectScalingFactor, objectScalingFactor, objectScalingFactor);
    Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, modelCube, 12);

    float angleY = (float) Math.random() * 80 - 40; // Angle in Y plane, between -40 and 40.
    angleY = (float) Math.toRadians(angleY);
    float newY = (float) Math.tan(angleY) * objectDistance;

//    modelPosition[0] = posVec[0];
//    modelPosition[1] = newY;
//    modelPosition[2] = posVec[2];
    modelPosition[0] = 0.0f;
    modelPosition[1] = 2.0f;
    modelPosition[2] = -10.0f;

    updateModelPosition();
  }

  /**
   * Check if user is looking at object by calculating where the object is in eye-space.
   *
   * @return true if the user is looking at the object.
   */
  private boolean isLookingAtObject() {
    // Convert object space to camera space. Use the headView from onNewFrame.
    Matrix.multiplyMM(modelView, 0, headView, 0, modelCube, 0);
    Matrix.multiplyMV(tempPosition, 0, modelView, 0, POS_MATRIX_MULTIPLY_VEC, 0);

    float pitch = (float) Math.atan2(tempPosition[1], -tempPosition[2]);
    float yaw = (float) Math.atan2(tempPosition[0], -tempPosition[2]);

    return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
  }

  // We receive all events from the Controller through this listener. In this example, our
  // listener handles both ControllerManager.EventListener and Controller.EventListener events.
  // This class is also a Runnable since the events will be reposted to the UI thread.
  private class EventListener extends Controller.EventListener
          implements ControllerManager.EventListener, Runnable {

    // The status of the overall controller API. This is primarily used for error handling since
    // it rarely changes.
    private String apiStatus;

    // The state of a specific Controller connection.
    private int controllerState = Controller.ConnectionStates.DISCONNECTED;

    @Override
    public void onApiStatusChanged(int state) {
      apiStatus = ControllerManager.ApiStatus.toString(state);
      uiHandler.post(this);
    }

    @Override
    public void onConnectionStateChanged(int state) {
      Log.i("ABC_state", String.valueOf(state));
      controllerState = state;
      uiHandler.post(this);
    }

    @Override
    public void onRecentered() {
      // In a real GVR application, this would have implicitly called recenterHeadTracker().
      // Most apps don't care about this, but apps that want to implement custom behavior when a
      // recentering occurs should use this callback.
//      controllerOrientationView.resetYaw();
    }

    @Override
    public void onUpdate() {
      uiHandler.post(this);
    }

    // Update the various TextViews in the UI thread.
    @Override
    public void run() {
      controller.update();

      float[] angles = new float[3];
      controller.orientation.toYawPitchRollDegrees(angles);

      if (controller.isTouching) {

        xTouch = controller.touch.x;
        yTouch = controller.touch.y;
      } else {
        xTouch = 0.5f;
        yTouch = 0.5f;
      }

      aButton = controller.appButtonState;
      bButton = controller.homeButtonState;
      yButton = controller.volumeUpButtonState;
      zButton = controller.volumeDownButtonState;

    }
  }

  @Override
  public void onStreamReceived(Session session, Stream stream) {
    Log.d("ABC", "onStreamReceived: New stream " + stream.getStreamId() + " in session " + session.getSessionId());

//    if (OpenTokConfig.SUBSCRIBE_TO_SELF) {
//      return;
//    }
    if (mSubscriber != null) {
      return;
    }

    subscribeToStream(stream);
  }

  @Override
  public void onStreamDropped(Session session, Stream stream) {
    Log.d("ABC", "onStreamDropped: Stream " + stream.getStreamId() + " dropped from session " + session.getSessionId());

//    if (OpenTokConfig.SUBSCRIBE_TO_SELF) {
//      return;
//    }
    if (mSubscriber == null) {
      return;
    }

    if (mSubscriber.getStream().equals(stream)) {
//      mSubscriberViewContainer.removeView(mSubscriber.getView());
      mSubscriber.destroy();
      mSubscriber = null;
    }
  }

  @Override
  public void onVideoDataReceived(SubscriberKit subscriberKit) {
    Log.d("ABC", "onVideoDataReceived ");
//    mSubscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
//    mSubscriberViewContainer.addView(mSubscriber.getView());
  }

  @Override
  public void onVideoDisabled(SubscriberKit subscriberKit, String s) {

    Log.d("ABC", "onVideoDisabled ");
  }

  @Override
  public void onVideoEnabled(SubscriberKit subscriberKit, String s) {

    Log.d("ABC", "onVideoEnabled ");
  }

  @Override
  public void onVideoDisableWarning(SubscriberKit subscriberKit) {

    Log.d("ABC", "onVideoDisableWarning ");
  }

  @Override
  public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {

    Log.d("ABC", "onVideoDisableWarningLifted ");
  }

  private void subscribeToStream(Stream stream) {
    Log.d("ABC", "subscribeToStream ");
//    mSubscriber = new Subscriber.Builder(TreasureHuntActivity.this, stream).build();
//    mSubscriber.setVideoListener(this);
//    mSession.subscribe(mSubscriber);
    mSubscriber = new Subscriber.Builder(this, stream).build();
    mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
    mSubscriber.setSubscriberListener(this);
    mSession.subscribe(mSubscriber);
//    mSubscriberViewContainer.addView(mSubscriber.getView());
  }

  private void disconnectSession() {
    Log.d("ABC", "disconnectSession ");
    if (mSession == null) {
      return;
    }

    if (mSubscriber != null) {
//      mSubscriberViewContainer.removeView(mSubscriber.getView());
      mSession.unsubscribe(mSubscriber);
      mSubscriber.destroy();
      mSubscriber = null;
    }

    mSession.disconnect();
  }

  @Override
  public void onConnected(SubscriberKit subscriberKit) {

    Log.d("ABC", "onConnected: Subscriber connected. Stream: "+subscriberKit.getStream().getStreamId());
  }

  @Override
  public void onDisconnected(SubscriberKit subscriberKit) {

    Log.d("ABC", "onDisconnected: Subscriber disconnected. Stream: "+subscriberKit.getStream().getStreamId());
  }

  @Override
  public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {

    Log.e("ABC", "onError: "+opentokError.getErrorDomain() + " : " +
            opentokError.getErrorCode() +  " - "+opentokError.getMessage());

  }

}
