package com.danopie

import com.danopie.method.action.init.ActionData
import com.danopie.method.action.data.GenerationData
import com.danopie.method.action.data.PerformAction
import com.danopie.method.action.utils.createJsonDeleteCall
import com.danopie.method.action.utils.selectFieldsWithDialog
import com.danopie.method.configuration.ConfigurationDataManager
import com.danopie.method.declaration.fullTypeName
import com.danopie.method.declaration.variableName
import com.danopie.method.ext.psi.extractClassName
import com.danopie.method.templater.*
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.danopie.method.action.BaseAnAction
import com.danopie.method.action.StaticActionProcessor

class JsonAction : BaseAnAction() {

    override fun processAction(
        event: AnActionEvent,
        actionData: ActionData,
        dartClass: DartClassDefinition
    ): PerformAction? {
        val declarations = selectFieldsWithDialog(actionData.project, dartClass) ?: return null

        return Companion.processAction(
            GenerationData(actionData, dartClass, declarations)
        )
    }

    companion object : StaticActionProcessor {

        override fun processAction(generationData: GenerationData): PerformAction {
            val (actionData, dartClass, declarations) = generationData

            val (project, _, _, _) = actionData

            val variableNames: List<AliasedVariableTemplateParam> = declarations
                .map {
                    AliasedVariableTemplateParamImpl(
                        variableName = it.variableName,
                        type = it.fullTypeName
                            ?: throw RuntimeException("No type is available - this variable should not be assignable from constructor"),
                        publicVariableName = it.publicVariableName
                    )
                }

            val templateManager = TemplateManager.getInstance(project)
            val configuration = ConfigurationDataManager.retrieveData(project)
            val dartClassName = dartClass.extractClassName()

            val template = createJsonTemplate(
                templateManager,
                JsonTemplateParams(
                    className = dartClassName,
                    variables = variableNames,
                    useNewKeyword = configuration.useNewKeyword,
                    addKeyMapper = configuration.addKeyMapperForMap,
                    noImplicitCasts = configuration.noImplicitCasts
                )
            )

            val deleteCall = createJsonDeleteCall(dartClass)

            return PerformAction(
                deleteCall,
                template
            )
        }

    }
}