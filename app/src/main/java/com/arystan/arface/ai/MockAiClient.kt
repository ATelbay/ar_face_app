package com.arystan.arface.ai

import com.arystan.arface.mask.MaskDescriptor

interface MockAiClient {
    suspend fun generateMask(prompt: String): MaskDescriptor
}
