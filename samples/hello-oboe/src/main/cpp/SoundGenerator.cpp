/*
 * Copyright 2018 The Android Open Source Project
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

#include <memory>
#include "SoundGenerator.h"

SoundGenerator::SoundGenerator(int32_t sampleRate, int32_t maxFrames, int32_t channelCount)
        : mSampleRate(sampleRate)
        , mChannelCount(channelCount)
        , mSineOscillators(std::make_unique<SineOscillator[]>(channelCount))
        , mManyToMultiConverter(channelCount)
        , mSinkFloat(channelCount)
        , mRampLinear(channelCount) {

    double frequency = 440.0;
    constexpr double interval = 110.0;
    constexpr float amplitude = 1.0;
    // Set up the oscillators
    for (int i = 0; i < mChannelCount; ++i) {
        mSineOscillators[i].frequency.setValue(frequency);
        mSineOscillators[i].amplitude.setValue(amplitude);
        mSineOscillators[i].setSampleRate(mSampleRate);
        mSineOscillators[i].output.connect(mManyToMultiConverter.inputs[i].get());
        frequency += interval;
    }
    mManyToMultiConverter.output.connect(&mRampLinear.input);
    mRampLinear.setTarget(0.0);
    mRampLinear.output.connect(&mSinkFloat.input);

}

void SoundGenerator::renderAudio(float *audioData, int32_t numFrames) {

    mFramePosition += mSinkFloat.read(mFramePosition, audioData, numFrames);
}

void SoundGenerator::setTonesOn(bool isOn) {
    if (isOn){
        mRampLinear.setTarget(1.0);
    } else {
        mRampLinear.setTarget(0.0);
    }
}
