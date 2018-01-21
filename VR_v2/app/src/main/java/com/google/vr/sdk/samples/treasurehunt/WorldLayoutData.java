/*
 * Copyright 2017 Google Inc. All Rights Reserved.

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

/**
 * Contains vertex, normal and color data.
 */
public final class WorldLayoutData {

  private static final float CUBE_SIZE = 3.0f;
  private static final float DRAW_SIZE = 0.5f;

  public static float[] getCubeCoords(float x, float y, float z) {
    float[] ret = {
            // Front face
            -DRAW_SIZE + x,  DRAW_SIZE + y, DRAW_SIZE + z,
            -DRAW_SIZE + x, -DRAW_SIZE + y, DRAW_SIZE + z,
            DRAW_SIZE + x,  DRAW_SIZE + y, DRAW_SIZE + z,
            -DRAW_SIZE + x, -DRAW_SIZE + y, DRAW_SIZE + z,
            DRAW_SIZE + x, -DRAW_SIZE + y, DRAW_SIZE + z,
            DRAW_SIZE + x,  DRAW_SIZE + y, DRAW_SIZE + z,

            // Right face
            DRAW_SIZE + x,  DRAW_SIZE + y,  DRAW_SIZE + z,
            DRAW_SIZE + x, -DRAW_SIZE + y,  DRAW_SIZE + z,
            DRAW_SIZE + x,  DRAW_SIZE + y, -DRAW_SIZE + z,
            DRAW_SIZE + x, -DRAW_SIZE + y,  DRAW_SIZE + z,
            DRAW_SIZE + x, -DRAW_SIZE + y, -DRAW_SIZE + z,
            DRAW_SIZE + x,  DRAW_SIZE + y, -DRAW_SIZE + z,

            // Back face
            DRAW_SIZE + x,  DRAW_SIZE + y, -DRAW_SIZE + z,
            DRAW_SIZE + x, -DRAW_SIZE + y, -DRAW_SIZE + z,
            -DRAW_SIZE + x,  DRAW_SIZE + y, -DRAW_SIZE + z,
            DRAW_SIZE + x, -DRAW_SIZE + y, -DRAW_SIZE + z,
            -DRAW_SIZE + x, -DRAW_SIZE + y, -DRAW_SIZE + z,
            -DRAW_SIZE + x,  DRAW_SIZE + y, -DRAW_SIZE + z,

            // Left face
            -DRAW_SIZE + x,  DRAW_SIZE + y, -DRAW_SIZE + z,
            -DRAW_SIZE + x, -DRAW_SIZE + y, -DRAW_SIZE + z,
            -DRAW_SIZE + x,  DRAW_SIZE + y,  DRAW_SIZE + z,
            -DRAW_SIZE + x, -DRAW_SIZE + y, -DRAW_SIZE + z,
            -DRAW_SIZE + x, -DRAW_SIZE + y,  DRAW_SIZE + z,
            -DRAW_SIZE + x,  DRAW_SIZE + y,  DRAW_SIZE + z,

            // Top face
            -DRAW_SIZE + x, DRAW_SIZE + y, -DRAW_SIZE + z,
            -DRAW_SIZE + x, DRAW_SIZE + y,  DRAW_SIZE + z,
            DRAW_SIZE + x, DRAW_SIZE + y, -DRAW_SIZE + z,
            -DRAW_SIZE + x, DRAW_SIZE + y,  DRAW_SIZE + z,
            DRAW_SIZE + x, DRAW_SIZE + y,  DRAW_SIZE + z,
            DRAW_SIZE + x, DRAW_SIZE + y, -DRAW_SIZE + z,

            // Bottom face
            DRAW_SIZE + x, -DRAW_SIZE + y, -DRAW_SIZE + z,
            DRAW_SIZE + x, -DRAW_SIZE + y,  DRAW_SIZE + z,
            -DRAW_SIZE + x, -DRAW_SIZE + y, -DRAW_SIZE + z,
            DRAW_SIZE + x, -DRAW_SIZE + y,  DRAW_SIZE + z,
            -DRAW_SIZE + x, -DRAW_SIZE + y,  DRAW_SIZE + z,
            -DRAW_SIZE + x, -DRAW_SIZE + y, -DRAW_SIZE + z,
    };

    return ret;
  }

  public static final float[] CUBE_COORDS = new float[] {
      // Front face
      -CUBE_SIZE, CUBE_SIZE, CUBE_SIZE,
      -CUBE_SIZE, -CUBE_SIZE, CUBE_SIZE,
      CUBE_SIZE, CUBE_SIZE, CUBE_SIZE,
      -CUBE_SIZE, -CUBE_SIZE, CUBE_SIZE,
      CUBE_SIZE, -CUBE_SIZE, CUBE_SIZE,
      CUBE_SIZE, CUBE_SIZE, CUBE_SIZE,

      // Right face
      CUBE_SIZE, CUBE_SIZE, CUBE_SIZE,
      CUBE_SIZE, -CUBE_SIZE, CUBE_SIZE,
      CUBE_SIZE, CUBE_SIZE, -CUBE_SIZE,
      CUBE_SIZE, -CUBE_SIZE, CUBE_SIZE,
      CUBE_SIZE, -CUBE_SIZE, -CUBE_SIZE,
      CUBE_SIZE, CUBE_SIZE, -CUBE_SIZE,

      // Back face
      CUBE_SIZE, CUBE_SIZE, -CUBE_SIZE,
      CUBE_SIZE, -CUBE_SIZE, -CUBE_SIZE,
      -CUBE_SIZE, CUBE_SIZE, -CUBE_SIZE,
      CUBE_SIZE, -CUBE_SIZE, -CUBE_SIZE,
      -CUBE_SIZE, -CUBE_SIZE, -CUBE_SIZE,
      -CUBE_SIZE, CUBE_SIZE, -CUBE_SIZE,

      // Left face
      -CUBE_SIZE, CUBE_SIZE, -CUBE_SIZE,
      -CUBE_SIZE, -CUBE_SIZE, -CUBE_SIZE,
      -CUBE_SIZE, CUBE_SIZE, CUBE_SIZE,
      -CUBE_SIZE, -CUBE_SIZE, -CUBE_SIZE,
      -CUBE_SIZE, -CUBE_SIZE, CUBE_SIZE,
      -CUBE_SIZE, CUBE_SIZE, CUBE_SIZE,

      // Top face
      -CUBE_SIZE, CUBE_SIZE, -CUBE_SIZE,
      -CUBE_SIZE, CUBE_SIZE, CUBE_SIZE,
      CUBE_SIZE, CUBE_SIZE, -CUBE_SIZE,
      -CUBE_SIZE, CUBE_SIZE, CUBE_SIZE,
      CUBE_SIZE, CUBE_SIZE, CUBE_SIZE,
      CUBE_SIZE, CUBE_SIZE, -CUBE_SIZE,

      // Bottom face
      CUBE_SIZE, -CUBE_SIZE, -CUBE_SIZE,
      CUBE_SIZE, -CUBE_SIZE, CUBE_SIZE,
      -CUBE_SIZE, -CUBE_SIZE, -CUBE_SIZE,
      CUBE_SIZE, -CUBE_SIZE, CUBE_SIZE,
      -CUBE_SIZE, -CUBE_SIZE, CUBE_SIZE,
      -CUBE_SIZE, -CUBE_SIZE, -CUBE_SIZE,
  };

  public static final float[] DRAW_COORDS = new float[] {
          // Front face
          -DRAW_SIZE, DRAW_SIZE, DRAW_SIZE,
          -DRAW_SIZE, -DRAW_SIZE, DRAW_SIZE,
          DRAW_SIZE, DRAW_SIZE, DRAW_SIZE,
          -DRAW_SIZE, -DRAW_SIZE, DRAW_SIZE,
          DRAW_SIZE, -DRAW_SIZE, DRAW_SIZE,
          DRAW_SIZE, DRAW_SIZE, DRAW_SIZE,

          // Right face
          DRAW_SIZE, DRAW_SIZE, DRAW_SIZE,
          DRAW_SIZE, -DRAW_SIZE, DRAW_SIZE,
          DRAW_SIZE, DRAW_SIZE, -DRAW_SIZE,
          DRAW_SIZE, -DRAW_SIZE, DRAW_SIZE,
          DRAW_SIZE, -DRAW_SIZE, -DRAW_SIZE,
          DRAW_SIZE, DRAW_SIZE, -DRAW_SIZE,

          // Back face
          DRAW_SIZE, DRAW_SIZE, -DRAW_SIZE,
          DRAW_SIZE, -DRAW_SIZE, -DRAW_SIZE,
          -DRAW_SIZE, DRAW_SIZE, -DRAW_SIZE,
          DRAW_SIZE, -DRAW_SIZE, -DRAW_SIZE,
          -DRAW_SIZE, -DRAW_SIZE, -DRAW_SIZE,
          -DRAW_SIZE, DRAW_SIZE, -DRAW_SIZE,

          // Left face
          -DRAW_SIZE, DRAW_SIZE, -DRAW_SIZE,
          -DRAW_SIZE, -DRAW_SIZE, -DRAW_SIZE,
          -DRAW_SIZE, DRAW_SIZE, DRAW_SIZE,
          -DRAW_SIZE, -DRAW_SIZE, -DRAW_SIZE,
          -DRAW_SIZE, -DRAW_SIZE, DRAW_SIZE,
          -DRAW_SIZE, DRAW_SIZE, DRAW_SIZE,

          // Top face
          -DRAW_SIZE, DRAW_SIZE, -DRAW_SIZE,
          -DRAW_SIZE, DRAW_SIZE, DRAW_SIZE,
          DRAW_SIZE, DRAW_SIZE, -DRAW_SIZE,
          -DRAW_SIZE, DRAW_SIZE, DRAW_SIZE,
          DRAW_SIZE, DRAW_SIZE, DRAW_SIZE,
          DRAW_SIZE, DRAW_SIZE, -DRAW_SIZE,

          // Bottom face
          DRAW_SIZE, -DRAW_SIZE, -DRAW_SIZE,
          DRAW_SIZE, -DRAW_SIZE, DRAW_SIZE,
          -DRAW_SIZE, -DRAW_SIZE, -DRAW_SIZE,
          DRAW_SIZE, -DRAW_SIZE, DRAW_SIZE,
          -DRAW_SIZE, -DRAW_SIZE, DRAW_SIZE,
          -DRAW_SIZE, -DRAW_SIZE, -DRAW_SIZE,
  };

  public static final float[] CUBE_COLORS = new float[] {
      // front, green
      0f, 0.5273f, 0.2656f, 1.0f,
      0f, 0.5273f, 0.2656f, 1.0f,
      0f, 0.5273f, 0.2656f, 1.0f,
      0f, 0.5273f, 0.2656f, 1.0f,
      0f, 0.5273f, 0.2656f, 1.0f,
      0f, 0.5273f, 0.2656f, 1.0f,

      // right, blue
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,

      // back, yellow
      0.9f, 0.9f, 0.2656f, 1.0f,
      0.9f, 0.9f, 0.2656f, 1.0f,
      0.9f, 0.9f, 0.2656f, 1.0f,
      0.9f, 0.9f, 0.2656f, 1.0f,
      0.9f, 0.9f, 0.2656f, 1.0f,
      0.9f, 0.9f, 0.2656f, 1.0f,

      // left, red
      0.8359375f,  0.17578125f,  0.125f, 1.0f,
      0.8359375f,  0.17578125f,  0.125f, 1.0f,
      0.8359375f,  0.17578125f,  0.125f, 1.0f,
      0.8359375f,  0.17578125f,  0.125f, 1.0f,
      0.8359375f,  0.17578125f,  0.125f, 1.0f,
      0.8359375f,  0.17578125f,  0.125f, 1.0f,

      // top, also blue
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,

      // bottom, also red
      0.8359375f,  0.17578125f,  0.125f, 1.0f,
      0.8359375f,  0.17578125f,  0.125f, 1.0f,
      0.8359375f,  0.17578125f,  0.125f, 1.0f,
      0.8359375f,  0.17578125f,  0.125f, 1.0f,
      0.8359375f,  0.17578125f,  0.125f, 1.0f,
      0.8359375f,  0.17578125f,  0.125f, 1.0f,
  };

  public static final float[] CUBE_FOUND_COLORS = new float[] {
      // front, yellow
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,

      // right, yellow
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,

      // back, yellow
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,

      // left, yellow
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,

      // top, yellow
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,

      // bottom, yellow
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
  };

  public static final float[] CUBE_NORMALS = new float[] {
      // Front face
      0.0f, 0.0f, 1.0f,
      0.0f, 0.0f, 1.0f,
      0.0f, 0.0f, 1.0f,
      0.0f, 0.0f, 1.0f,
      0.0f, 0.0f, 1.0f,
      0.0f, 0.0f, 1.0f,

      // Right face
      1.0f, 0.0f, 0.0f,
      1.0f, 0.0f, 0.0f,
      1.0f, 0.0f, 0.0f,
      1.0f, 0.0f, 0.0f,
      1.0f, 0.0f, 0.0f,
      1.0f, 0.0f, 0.0f,

      // Back face
      0.0f, 0.0f, -1.0f,
      0.0f, 0.0f, -1.0f,
      0.0f, 0.0f, -1.0f,
      0.0f, 0.0f, -1.0f,
      0.0f, 0.0f, -1.0f,
      0.0f, 0.0f, -1.0f,

      // Left face
      -1.0f, 0.0f, 0.0f,
      -1.0f, 0.0f, 0.0f,
      -1.0f, 0.0f, 0.0f,
      -1.0f, 0.0f, 0.0f,
      -1.0f, 0.0f, 0.0f,
      -1.0f, 0.0f, 0.0f,

      // Top face
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,

      // Bottom face
      0.0f, -1.0f, 0.0f,
      0.0f, -1.0f, 0.0f,
      0.0f, -1.0f, 0.0f,
      0.0f, -1.0f, 0.0f,
      0.0f, -1.0f, 0.0f,
      0.0f, -1.0f, 0.0f
  };

  // The grid lines on the floor are rendered procedurally and large polygons cause floating point
  // precision problems on some architectures. So we split the floor into 4 quadrants.
  public static final float[] FLOOR_COORDS = new float[] {
      // +X, +Z quadrant
      200, 0, 0,
      0, 0, 0,
      0, 0, 200,
      200, 0, 0,
      0, 0, 200,
      200, 0, 200,

      // -X, +Z quadrant
      0, 0, 0,
      -200, 0, 0,
      -200, 0, 200,
      0, 0, 0,
      -200, 0, 200,
      0, 0, 200,

      // +X, -Z quadrant
      200, 0, -200,
      0, 0, -200,
      0, 0, 0,
      200, 0, -200,
      0, 0, 0,
      200, 0, 0,

      // -X, -Z quadrant
      0, 0, -200,
      -200, 0, -200,
      -200, 0, 0,
      0, 0, -200,
      -200, 0, 0,
      0, 0, 0,
  };

  public static final float[] FLOOR_NORMALS = new float[] {
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
  };

  public static final float[] FLOOR_COLORS = new float[] {
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
  };
}
