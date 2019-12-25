package com.danopie.method.action.my

import com.danopie.method.action.StaticActionProcessor
import com.danopie.method.action.data.GenerationData
import com.danopie.method.action.data.PerformAction
import com.danopie.method.declaration.variableName
import com.danopie.method.ext.psi.findChildrenByType
import com.danopie.method.templater.HashCodeTemplateParams
import com.danopie.method.templater.NamedVariableTemplateParamImpl
import com.danopie.method.templater.TemplateConstants
import com.danopie.method.templater.createHashCodeTemplate
import com.intellij.codeInsight.template.TemplateManager
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.jetbrains.lang.dart.psi.DartGetterDeclaration

class MyHashCodeAction {

    companion object : StaticActionProcessor {

        private fun createDeleteCall(dartClass: DartClassDefinition): (() -> Unit)? {
            val hashCode = dartClass.findChildrenByType<DartGetterDeclaration>()
                .filter { it.name == TemplateConstants.HASHCODE_NAME }
                .firstOrNull()
                ?: return null

            return { hashCode.delete() }
        }

        override fun processAction(generationData: GenerationData): PerformAction? {
            val (actionData, dartClass, declarations) = generationData

            val project = actionData.project

            val templateManager = TemplateManager.getInstance(project)

            val template = createHashCodeTemplate(
                templateManager = templateManager,
                params = HashCodeTemplateParams(
                    declarations.map { NamedVariableTemplateParamImpl(it.variableName) }
                )
            )

            return PerformAction(
                createDeleteCall(dartClass),
                template
            )
        }
    }
}
