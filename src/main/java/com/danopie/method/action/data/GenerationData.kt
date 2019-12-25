package com.danopie.method.action.data

import com.danopie.method.action.init.ActionData
import com.danopie.method.declaration.VariableDeclaration
import com.jetbrains.lang.dart.psi.DartClassDefinition

data class GenerationData(
    val actionData: ActionData,
    val dartClass: DartClassDefinition,
    val declarations: List<VariableDeclaration>
)