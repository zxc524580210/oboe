/**
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

#include <trace.h>
#include <inttypes.h>
#include <memory>

#include <Oscillator.h>

#include "HelloOboeEngine.h"
#include "SoundGenerator.h"



HelloOboeEngine::HelloOboeEngine() : mLatencyCallback(std::make_shared<LatencyTuningCallback>()){

     mAudioEngine = std::make_unique<AudioEngine>(mLatencyCallback);
     mAudioSource =  std::make_shared<SoundGenerator>(
             mAudioEngine->getStream()->getSampleRate(), // TODO: Figure out how to expose stream properties from AudioEngine
             mAudioEngine->getStream()->getChannelCount());
     mLatencyCallback->setSource(mAudioSource);

     mAudioEngine->startPlaybackStream();

    // Initialize the trace functions, this enables you to output trace statements without
    // blocking. See https://developer.android.com/studio/profile/systrace-commandline.html
     Trace::initialize();
     updateLatencyDetection();
}

double HelloOboeEngine::getCurrentOutputLatencyMillis() {
    if (!mIsLatencyDetectionSupported) return -1;
    // Get the time that a known audio frame was presented for playing
    auto result = mAudioEngine->getStream()->getTimestamp(CLOCK_MONOTONIC);
    double outputLatencyMillis = -1;
    const int64_t kNanosPerMillisecond = 1000000;
    if (result == oboe::Result::OK) {
        oboe::FrameTimestamp playedFrame = result.value();
        // Get the write index for the next audio frame
        int64_t writeIndex = mAudioEngine->getStream()->getFramesWritten();
        // Calculate the number of frames between our known frame and the write index
        int64_t frameIndexDelta = writeIndex - playedFrame.position;
        // Calculate the time which the next frame will be presented
        int64_t frameTimeDelta = (frameIndexDelta * oboe::kNanosPerSecond) /  (mAudioEngine->getStream()->getSampleRate());
        int64_t nextFramePresentationTime = playedFrame.timestamp + frameTimeDelta;
        // Assume that the next frame will be written at the current time
        using namespace std::chrono;
        int64_t nextFrameWriteTime =
                duration_cast<nanoseconds>(steady_clock::now().time_since_epoch()).count();
        // Calculate the latency
        outputLatencyMillis = static_cast<double>(nextFramePresentationTime - nextFrameWriteTime)
                         / kNanosPerMillisecond;
    } else {
        LOGE("Error calculating latency: %s", oboe::convertToText(result.error()));
    }
    return outputLatencyMillis;
}

void HelloOboeEngine::setBufferSizeInBursts(int32_t numBursts) {
    mIsLatencyDetectionSupported = false;
    mLatencyCallback->setBufferTuneEnabled(numBursts == kBufferSizeAutomatic);
    auto result = mAudioEngine->getStream()->setBufferSizeInFrames(
            numBursts * mAudioEngine->getStream()->getFramesPerBurst());
    updateLatencyDetection();
    if (result) {
        LOGD("Buffer size successfully changed to %d", result.value());
    } else {
        LOGW("Buffer size could not be changed, %d", result.error());
    }
}

void HelloOboeEngine::setAudioApi(oboe::AudioApi audioApi) {
    mIsLatencyDetectionSupported = false;
    mAudioEngine->createCustomStream(std::move(*oboe::AudioStreamBuilder(*mAudioEngine->getStream())
            .setAudioApi((audioApi))));
    updateAudioSource();
    LOGD("AudioAPI is now %d", mAudioEngine->getStream()->getAudioApi());
}

void HelloOboeEngine::setChannelCount(int channelCount) {
    mIsLatencyDetectionSupported = false;
    mAudioEngine->createCustomStream(std::move(*oboe::AudioStreamBuilder(*mAudioEngine->getStream())
            .setChannelCount(channelCount)));
    updateAudioSource();
    LOGD("Channel count is now %d", mAudioEngine->getStream()->getChannelCount());
}

void HelloOboeEngine::setDeviceId(int32_t deviceId) {
    mIsLatencyDetectionSupported = false;
    mAudioEngine->createCustomStream(std::move(*oboe::AudioStreamBuilder(*mAudioEngine->getStream()).
            setDeviceId(deviceId)));
    updateAudioSource();
    LOGD("Device ID is now %d", mAudioEngine->getStream()->getDeviceId());
}

bool HelloOboeEngine::isLatencyDetectionSupported() {
    return mIsLatencyDetectionSupported;
}

void HelloOboeEngine::updateLatencyDetection() {
    mIsLatencyDetectionSupported = (mAudioEngine->getStream()->getTimestamp((CLOCK_MONOTONIC)) !=
                                    oboe::Result::ErrorUnimplemented);
}

void HelloOboeEngine::tap(bool isDown) {
    mAudioSource->tap(isDown);
}

void HelloOboeEngine::updateAudioSource() {
    *mAudioSource = SoundGenerator(mAudioEngine->getStream()->getSampleRate(), mAudioEngine->getStream()->getChannelCount());
    mAudioEngine->startPlaybackStream();
    updateLatencyDetection();
}

