/*
 * Copyright 2014-2016 Media for Mobile
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

package org.m4m.domain;

public class Pair<T, U> {
    public T left;
    public U right;

    public Pair(T left, U right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "(" + (left == null ? "NULL" : left.toString()) + ", " + (right == null ? "NULL" : right.toString()) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;

        Pair pair = (Pair) o;

        if ((left == null && pair.left != null) || (left != null && pair.left == null) || (left != null && !left.equals(pair.left)))
            return false;
        if ((right == null && pair.right != null) || (right != null && pair.right == null) || (right != null && !right.equals(pair.right)))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (left != null) result += left.hashCode();
        if (right != null) result = 31 * result + right.hashCode();
        return result;
    }
}
