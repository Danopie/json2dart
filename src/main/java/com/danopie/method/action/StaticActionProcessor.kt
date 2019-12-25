package com.danopie.method.action

import com.danopie.method.action.data.GenerationData
import com.danopie.method.action.data.PerformAction

interface StaticActionProcessor {

    fun processAction(generationData: GenerationData): PerformAction?

}
