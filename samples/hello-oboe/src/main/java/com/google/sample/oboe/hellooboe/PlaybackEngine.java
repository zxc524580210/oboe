package com.google.sample.oboe.hellooboe;
/*
 * Copyright 2017 The Android Open Source Project
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

public class PlaybackEngine {

    static long mEngineHandle = 0;

    // Load native library
    static {
        System.loadLibrary("hello-oboe");
    }

    static boolean create(){

        if (mEngineHandle == 0){
            mEngineHandle = native_createEngine();
        }
        return (mEngineHandle != 0);
    }

    static void delete(){
        if (mEngineHandle != 0){
            native_deleteEngine(mEngineHandle);
        }
        mEngineHandle = 0;
    }

    static void setToneOn(boolean isToneOn){
        if (mEngineHandle != 0) native_setToneOn(mEngineHandle, isToneOn);
    }

    static void setChannelOn1(boolean isChannelOn){
        if (mEngineHandle != 0) native_setChannelOn1(mEngineHandle, isChannelOn);
    }

    static void setChannelOn2(boolean isChannelOn){
        if (mEngineHandle != 0) native_setChannelOn2(mEngineHandle, isChannelOn);
    }

    static void setChannelOn3(boolean isChannelOn){
        if (mEngineHandle != 0) native_setChannelOn3(mEngineHandle, isChannelOn);
    }

    static void setChannelOn4(boolean isChannelOn){
        if (mEngineHandle != 0) native_setChannelOn4(mEngineHandle, isChannelOn);
    }

    static void setChannelOn5(boolean isChannelOn){
        if (mEngineHandle != 0) native_setChannelOn5(mEngineHandle, isChannelOn);
    }

    static void setChannelOn6(boolean isChannelOn){
        if (mEngineHandle != 0) native_setChannelOn6(mEngineHandle, isChannelOn);
    }

    static void setChannelOn7(boolean isChannelOn){
        if (mEngineHandle != 0) native_setChannelOn7(mEngineHandle, isChannelOn);
    }

    static void setChannelOn8(boolean isChannelOn){
        if (mEngineHandle != 0) native_setChannelOn8(mEngineHandle, isChannelOn);
    }

    static void setAudioApi(int audioApi){
        if (mEngineHandle != 0) native_setAudioApi(mEngineHandle, audioApi);
    }

    static void setAudioDeviceId(int deviceId){
        if (mEngineHandle != 0) native_setAudioDeviceId(mEngineHandle, deviceId);
    }

    static void setChannelCount(int channelCount) {
        if (mEngineHandle != 0) native_setChannelCount(mEngineHandle, channelCount);
    }

    static void setBufferSizeInBursts(int bufferSizeInBursts){
        if (mEngineHandle != 0) native_setBufferSizeInBursts(mEngineHandle, bufferSizeInBursts);
    }

    static double getCurrentOutputLatencyMillis(){
        if (mEngineHandle == 0) return 0;
        return native_getCurrentOutputLatencyMillis(mEngineHandle);
    }

    static boolean isLatencyDetectionSupported() {
        return mEngineHandle != 0 && native_isLatencyDetectionSupported(mEngineHandle);
    }

    // Native methods
    private static native long native_createEngine();
    private static native void native_deleteEngine(long engineHandle);
    private static native void native_setToneOn(long engineHandle, boolean isToneOn);
    private static native void native_setChannelOn1(long engineHandle, boolean isChannelOn);
    private static native void native_setChannelOn2(long engineHandle, boolean isChannelOn);
    private static native void native_setChannelOn3(long engineHandle, boolean isChannelOn);
    private static native void native_setChannelOn4(long engineHandle, boolean isChannelOn);
    private static native void native_setChannelOn5(long engineHandle, boolean isChannelOn);
    private static native void native_setChannelOn6(long engineHandle, boolean isChannelOn);
    private static native void native_setChannelOn7(long engineHandle, boolean isChannelOn);
    private static native void native_setChannelOn8(long engineHandle, boolean isChannelOn);
    private static native void native_setAudioApi(long engineHandle, int audioApi);
    private static native void native_setAudioDeviceId(long engineHandle, int deviceId);
    private static native void native_setChannelCount(long mEngineHandle, int channelCount);
    private static native void native_setBufferSizeInBursts(long engineHandle, int bufferSizeInBursts);
    private static native double native_getCurrentOutputLatencyMillis(long engineHandle);
    private static native boolean native_isLatencyDetectionSupported(long engineHandle);
}
