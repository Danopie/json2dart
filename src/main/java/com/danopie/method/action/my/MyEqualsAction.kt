package com.danopie.method.action.my

import com.danopie.method.action.StaticActionProcessor
import com.danopie.method.action.data.GenerationData
import com.danopie.method.action.data.PerformAction
import com.danopie.method.declaration.variableName
import com.danopie.method.ext.psi.extractClassName
import com.danopie.method.ext.psi.findMethodsByName
import com.danopie.method.templater.EqualsTemplateParams
import com.danopie.method.templater.NamedVariableTemplateParamImpl
import com.danopie.method.templater.TemplateConstants
import com.danopie.method.templater.createEqualsTemplate
import com.intellij.codeInsight.template.TemplateManager
import com.jetbrains.lang.dart.psi.DartClassDefinition

class MyEqualsAction {

    companion object : StaticActionProcessor {

        private fun createDeleteCall(dartClass: DartClassDefinition): (() -> Unit)? {
            val equals = dartClass.findMethodsByName(TemplateConstants.EQUALS_OPERATOR_METHOD_NAME)
                .firstOrNull()
                ?: return null

            return { equals.delete() }
        }

        override fun processAction(generationData: GenerationData): PerformAction? {
            val (actionData, dartClass, declarations) = generationData

            val project = actionData.project

            val templateManager = TemplateManager.getInstance(project)
            val dartClassName = dartClass.extractClassName()

            val template = createEqualsTemplate(
                templateManager,
                EqualsTemplateParams(
                    className = dartClassName,
                    variables = declarations.map { NamedVariableTemplateParamImpl(it.variableName) }
                )
            )

            return PerformAction(
                createDeleteCall(dartClass),
                template
            )
        }
    }

}