package com.arystan.arface.mask

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Wave 0 placeholder. Replaced by MaskRepositoryImpl after Wave 2 merge. */
class StubMaskRepository : MaskRepository {
    private val _masks = MutableStateFlow<List<MaskDescriptor>>(emptyList())

    override fun getAllMasks(): StateFlow<List<MaskDescriptor>> = _masks.asStateFlow()

    override fun addGeneratedMask(descriptor: MaskDescriptor) {
        _masks.value = _masks.value + descriptor
    }
}
