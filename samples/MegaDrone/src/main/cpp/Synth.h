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
#include "../../../../../src/common/OboeDebug.h"

#include "shared/Oscillator.h"
#include "shared/Mixer.h"
#include "shared/MonoToStereo.h"
#include "shared/FlowMixer.h"
#include "shared/SineOscillator.h"
#include "../../../../../src/flowgraph/SinkFloat.h"

constexpr int kNumOscillators = 2;
//constexpr float kOscBaseFrequency = 116.0;
constexpr float kOscBaseFrequency = 440.0;
constexpr float kOscDivisor = 33;
constexpr float kOscAmplitude = 0.009;
constexpr int kChannelCount = 1;

/**
 * TODO: Pitch is half what is expected
 */
class Synth : public IRenderableAudio {
public:

    Synth(int32_t sampleRate, int32_t channelCount)
    : mFlowMixer(kNumOscillators, kChannelCount)
    , mFlowSink(kChannelCount)
    {

        mFlowOscs[0].setSampleRate(sampleRate);
        mFlowOscs[0].frequency.setValue(440.0);
        mFlowOscs[0].amplitude.setValue(0.5);
        mFlowOscs[0].output.connect(mFlowMixer.inputs[0].get());

        mFlowOscs[1].setSampleRate(sampleRate);
        mFlowOscs[1].frequency.setValue(880.0);
        mFlowOscs[1].amplitude.setValue(0.5);
        mFlowOscs[1].output.connect(mFlowMixer.inputs[1].get());

        for (int i = 0; i < kNumOscillators; ++i) {

            // TODO: Can maybe remove static_cast
            mFlowOscs[i].setSampleRate(sampleRate);
            mFlowOscs[i].frequency.setValue(kOscBaseFrequency+(static_cast<float>(i)/kOscDivisor));
            //mFlowOscs[i].frequency.setValue(kOscBaseFrequency);
            mFlowOscs[i].amplitude.setValue(kOscAmplitude);
            mFlowOscs[i].output.connect(mFlowMixer.inputs[i].get());
        }

        /*if (channelCount == oboe::ChannelCount::Stereo){
            mOutputStage = std::make_shared<MonoToStereo>(&mMixer);
        } else {
            mOutputStage.reset(&mMixer);
        }*/


        mFlowMixer.output.connect(&mFlowSink.input);

        //mFlowOscs[0].output.connect(&mFlowSink.input);
    }

    // From ISynth
    void setWaveOn(bool isEnabled) {

    };

    // From IRenderableAudio
    void renderAudio(float *audioData, int32_t numFrames) override {

        /**
         * Here's why we were hearing a high pitched squeal rather than a nice 440Hz sine wave:
         *
         * Sink takes a read position which we were specifying zero, this read position is passed
         * to AudioProcessorBase::pullData which checks that the specified read position is greater
         * than the previously specified read position and only actually pulls the data if it is,
         * otherwise it just returns the existing buffer (which defaults to size 8). This was
         * causing the SinkFloat::read to copy the same 8 floats into memory in a loop
         *
         * TODO: Perhaps have an implementation of sink which keeps track of its own readPosition
         * so we don't have to
         */
        mReadPosition += mFlowSink.read(mReadPosition, audioData, numFrames);
    };

    virtual ~Synth() {}
private:

    // Rendering objects
    std::array<SineOscillator, kNumOscillators> mFlowOscs;
    FlowMixer mFlowMixer;
    SinkFloat mFlowSink;
    int64_t mReadPosition = 0;

};


#endif //MEGADRONE_SYNTH_H
