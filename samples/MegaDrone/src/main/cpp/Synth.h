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

#ifndef MEGADRONE_SYNTH_H
#define MEGADRONE_SYNTH_H

#include <array>
#include <RampLinear.h>

#include "shared/Oscillator.h"
#include "shared/Mixer.h"
#include "shared/MonoToStereo.h"
#include "shared/FlowMixer.h"
#include "shared/SquareOscillator.h"
#include "../../../../../src/flowgraph/SinkFloat.h"

constexpr int kNumOscillators = 100;
constexpr float kOscBaseFrequency = 116.0;
constexpr float kOscDivisor = 33;
constexpr float kOscAmplitude = 0.009;
constexpr int kChannelCount = 1;


class Synth : public IRenderableAudio {
public:

    Synth(int32_t sampleRate, int32_t channelCount)
    : mFlowMixer(kNumOscillators, kChannelCount)
    , mFlowSink(kChannelCount)
    , mRampLinear(kChannelCount)
    {

        for (int i = 0; i < kNumOscillators; ++i) {
            mFlowOscs[i].setSampleRate(sampleRate);
            mFlowOscs[i].frequency.setValue(kOscBaseFrequency+(i/kOscDivisor));
            mFlowOscs[i].amplitude.setValue(kOscAmplitude);
            mFlowOscs[i].output.connect(mFlowMixer.inputs[i].get());
        }

        // TODO: This should no longer be necessary when Phil commits mono->stereo conversion
        /*if (channelCount == oboe::ChannelCount::Stereo){
            mOutputStage = std::make_shared<MonoToStereo>(&mMixer);
        } else {
            mOutputStage.reset(&mMixer);
        }*/

        mRampLinear.setTarget(0); // Set initial output to silence
        mFlowMixer.output.connect(&mRampLinear.input);
        mRampLinear.output.connect(&mFlowSink.input);
    }

    // From ISynth
    void setWaveOn(bool isEnabled) {
        if (isEnabled){
            mRampLinear.setTarget(1.0);
        } else {
            mRampLinear.setTarget(0);
        }
    };

    // From IRenderableAudio
    void renderAudio(float *audioData, int32_t numFrames) override {
        mReadPosition += mFlowSink.read(mReadPosition, audioData, numFrames);
    };

    virtual ~Synth() {}
private:

    // Rendering objects
    std::array<SquareOscillator, kNumOscillators> mFlowOscs;
    FlowMixer mFlowMixer; // Mixes
    SinkFloat mFlowSink;
    RampLinear mRampLinear;
    int64_t mReadPosition = 0;
};


#endif //MEGADRONE_SYNTH_H
