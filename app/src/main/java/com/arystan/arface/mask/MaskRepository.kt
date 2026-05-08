package com.arystan.arface.mask

import kotlinx.coroutines.flow.StateFlow

interface MaskRepository {
    fun getAllMasks(): StateFlow<List<MaskDescriptor>>
    fun addGeneratedMask(descriptor: MaskDescriptor)
}
