/**
 * Copyright 2015 Dean Wild
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.liceoarzignano.bold.external.showcase.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import it.liceoarzignano.bold.external.showcase.target.Target;

public class CircleShape implements Shape {

    private int radius = 200;

    private CircleShape(int radius) {
        this.radius = radius;
    }

    private CircleShape(Rect bounds) {
        this(getPreferredRadius(bounds));
    }

    public CircleShape(Target target) {
        this(target.getBounds());
    }

    private static int getPreferredRadius(Rect bounds) {
        return Math.max(bounds.width(), bounds.height()) / 2;
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int x, int y, int padding) {
        if (radius > 0) {
            canvas.drawCircle(x, y, radius + padding, paint);
        }
    }

    @Override
    public void updateTarget(Target target) {
        radius = getPreferredRadius(target.getBounds());
    }

    @Override
    public int getHeight() {
        return radius * 2;
    }
}
