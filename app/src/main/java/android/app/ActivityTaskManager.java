/*
 * Copyright (C) 2018 The Android Open Source Project
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

package android.app;

import android.graphics.Rect;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

public class ActivityTaskManager {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static class RootTaskInfo extends TaskInfo implements Parcelable {
        // TODO(b/148895075): Move some of the fields to TaskInfo.
        public Rect bounds;
        public int[] childTaskIds;
        public String[] childTaskNames;
        public Rect[] childTaskBounds;
        public int[] childTaskUserIds;
        public boolean visible;
        // Index of the stack in the display's stack list, can be used for comparison of stack order
        public int position;

        @Override
        public int describeContents() {
            return 0;
        }

        protected RootTaskInfo(Parcel source) {
            bounds = source.readTypedObject(Rect.CREATOR);
            childTaskIds = source.createIntArray();
            childTaskNames = source.createStringArray();
            childTaskBounds = source.createTypedArray(Rect.CREATOR);
            childTaskUserIds = source.createIntArray();
            visible = source.readInt() > 0;
            position = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedObject(bounds, flags);
            dest.writeIntArray(childTaskIds);
            dest.writeStringArray(childTaskNames);
            dest.writeTypedArray(childTaskBounds, flags);
            dest.writeIntArray(childTaskUserIds);
            dest.writeInt(visible ? 1 : 0);
            dest.writeInt(position);
        }

        public static final Creator<RootTaskInfo> CREATOR = new Creator<>() {
            @Override
            public RootTaskInfo createFromParcel(Parcel in) {
                return new RootTaskInfo(in);
            }

            @Override
            public RootTaskInfo[] newArray(int size) {
                return new RootTaskInfo[size];
            }
        };
    }
}
