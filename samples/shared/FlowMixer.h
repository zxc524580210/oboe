/*
 * Copyright 2019 The Android Open Source Project
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

#ifndef SAMPLES_FLOWMIXER_H
#define SAMPLES_FLOWMIXER_H

#include "../../../oboe/src/flowgraph/AudioProcessorBase.h"

using namespace flowgraph;

class FlowMixer : public AudioProcessorBase {
public:
    FlowMixer(int32_t inputCount, int32_t samplesPerFrame)
    : inputs(inputCount)
    , output (*this, samplesPerFrame)
    {
        for (int i = 0; i < inputCount; i++) {
            inputs[i] = (std::make_unique<AudioFloatInputPort>(*this, samplesPerFrame));
        }
    }

    int32_t onProcess(int32_t numFrames){

        float *mixingBuffer = output.getBuffer();
        memset(mixingBuffer, 0, sizeof(float) * numFrames * output.getSamplesPerFrame());

        for (auto &inputPort : inputs){

            float *inputBuffer = inputPort->getBuffer();

            for (int i = 0; i < numFrames; i++){
                mixingBuffer[i] += inputBuffer[i];
            }
        }
        return numFrames;
    }

    std::vector<std::unique_ptr<flowgraph::AudioFloatInputPort>> inputs;
    flowgraph::AudioFloatOutputPort output;

};


#endif //SAMPLES_FLOWMIXER_H
