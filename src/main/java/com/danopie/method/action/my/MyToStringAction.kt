package com.danopie.method.action.my

import com.danopie.method.action.StaticActionProcessor
import com.danopie.method.action.data.GenerationData
import com.danopie.method.action.data.PerformAction
import com.danopie.method.declaration.variableName
import com.danopie.method.ext.psi.extractClassName
import com.danopie.method.ext.psi.findMethodsByName
import com.danopie.method.templater.NamedVariableTemplateParamImpl
import com.danopie.method.templater.TemplateConstants
import com.danopie.method.templater.ToStringTemplateParams
import com.danopie.method.templater.createToStringTemplate
import com.intellij.codeInsight.template.TemplateManager
import com.jetbrains.lang.dart.psi.DartClassDefinition

class MyToStringAction {
    companion object : StaticActionProcessor {

        private fun createDeleteCall(dartClass: DartClassDefinition): (() -> Unit)? {
            val toString = dartClass.findMethodsByName(TemplateConstants.TO_STRING_METHOD_NAME)
                .firstOrNull()
                ?: return null

            return { toString.delete() }
        }

        override fun processAction(generationData: GenerationData): PerformAction? {
            val (actionData, dartClass, declarations) = generationData

            val project = actionData.project

            val templateManager = TemplateManager.getInstance(project)
            val dartClassName = dartClass.extractClassName()

            val template = createToStringTemplate(
                templateManager = templateManager,
                params = ToStringTemplateParams(
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
