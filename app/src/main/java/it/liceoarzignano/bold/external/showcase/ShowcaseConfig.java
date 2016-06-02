/**
 * Copyright 2015 Dean Wild
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.liceoarzignano.bold.external.showcase;

import it.liceoarzignano.bold.external.showcase.shape.CircleShape;
import it.liceoarzignano.bold.external.showcase.shape.Shape;

@SuppressWarnings("unused")
class ShowcaseConfig {

    static final long DEFAULT_FADE_TIME = 300;
    static final long DEFAULT_DELAY = 20;
    private static final Shape DEFAULT_SHAPE = new CircleShape();
    static final int DEFAULT_SHAPE_PADDING = 10;

    private Shape mShape = DEFAULT_SHAPE;

    public ShowcaseConfig() {
    }

    public Shape getShape() {
        return mShape;
    }

    public void setShape(Shape shape) {
        this.mShape = shape;
    }
}
