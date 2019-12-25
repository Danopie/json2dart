package com.danopie.method.templater

import com.danopie.method.ext.*
import com.danopie.method.utils.TypeUtils
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager

data class JsonTemplateParams(
    val className: String,
    val variables: List<AliasedVariableTemplateParam>,
    val useNewKeyword: Boolean,
    val addKeyMapper: Boolean,
    val noImplicitCasts: Boolean
)

// The 2 will be generated with the same function
fun createJsonTemplate(
    templateManager: TemplateManager,
    params: JsonTemplateParams
): Template {

    return templateManager.createTemplate(
        TemplateType.JsonTemplate.templateKey,
        TemplateConstants.DART_TEMPLATE_GROUP
    ).apply {
        addToJson(params)
        addNewLine()
        addNewLine()
        addFromJson(params)
    }
}

private fun Template.addAssignKeyMapperIfNotValid() {
    addTextSegment(TemplateConstants.KEYMAPPER_VARIABLE_NAME)
    addSpace()
    addTextSegment("??=")
    addSpace()
    withParentheses {
        addTextSegment(TemplateConstants.KEY_VARIABLE_NAME)
    }
    addSpace()
    addTextSegment("=>")
    addSpace()
    addTextSegment(TemplateConstants.KEY_VARIABLE_NAME)
    addSemicolon()
    addNewLine()
    addNewLine()
}

private fun Template.addToJson(params: JsonTemplateParams) {
    val (_, variables, _, addKeyMapper, _) = params

    isToReformat = true

    addTextSegment("Map<String, dynamic>")
    addSpace()
    addTextSegment(TemplateConstants.TO_JSON_METHOD_NAME)
    withParentheses {
        if (addKeyMapper) {
            withCurlyBraces {
                addNewLine()
                addTextSegment("String ${TemplateConstants.KEYMAPPER_VARIABLE_NAME}(String key)")
                addComma()
                addNewLine()
            }
        }
    }
    addSpace()
    withCurlyBraces {

        if (addKeyMapper) {
            addAssignKeyMapperIfNotValid()
        }

        addTextSegment("return")
        addSpace()
        withCurlyBraces {
            addNewLine()

            variables.forEach {
                "'${it.mapKeyString}'".also { keyParam ->
                    if (addKeyMapper) {
                        addTextSegment(TemplateConstants.KEYMAPPER_VARIABLE_NAME)
                        withParentheses {
                            addTextSegment(keyParam)
                        }
                    } else {
                        addTextSegment(keyParam)
                    }
                }

                addTextSegment(":")
                addSpace()
                addTextSegment(it.variableName)
                addSpace()
                addTextSegment("==")
                addSpace()
                addTextSegment("null")
                addSpace()
                addTextSegment("?")
                addSpace()
                addTextSegment("null")
                addSpace()
                addTextSegment(":")
                addSpace()
                addToJsonTypeText(it.variableName, it.type)
                addComma()
                addNewLine()
            }
        }
        addSemicolon()
    }
}

private fun Template.addToJsonTypeText(variableName: String, variableType: String){
    if(TypeUtils.isCollection(variableType)){
        val collectionType = TypeUtils.getCollectionType(variableType)
        val childType = TypeUtils.getCollectionChildType(variableType)
        addTextSegment("$collectionType<dynamic>.from($variableName.map((x)")
        addSpace()
        addTextSegment("=>")
        addSpace()
        if(TypeUtils.isPrimitive(childType)){
            addTextSegment("x))")
        } else {
            addTextSegment("x.${TemplateConstants.TO_JSON_METHOD_NAME}()))")
        }
    } else {
        if(TypeUtils.isPrimitive(variableType)){
            addTextSegment(variableName)
        } else {
            addTextSegment("$variableName.${TemplateConstants.TO_JSON_METHOD_NAME}()")
        }
    }

}

private fun Template.addFromJson(
    params: JsonTemplateParams
) {
    val (className, variables, useNewKeyword, addKeyMapper, noImplicitCasts) = params

    isToReformat = true

    addTextSegment("factory")
    addSpace()
    addTextSegment(className)
    addTextSegment(".")
    addTextSegment(TemplateConstants.FROM_JSON_METHOD_NAME)
    withParentheses {
        if (addKeyMapper) {
            addNewLine()
            // New line does not format, no matter what is in this if statement
            addSpace()
        }
        addTextSegment("Map<String, dynamic>")
        addSpace()
        addTextSegment(TemplateConstants.JSON_VARIABLE_NAME)

        if (addKeyMapper) {
            addComma()
            addSpace()
            withCurlyBraces {
                addNewLine()
                addTextSegment("String")
                addSpace()
                addTextSegment(TemplateConstants.KEYMAPPER_VARIABLE_NAME)
                withParentheses {
                    addTextSegment("String")
                    addSpace()
                    addTextSegment(TemplateConstants.KEY_VARIABLE_NAME)
                }
                addComma()
                addNewLine()
            }
        }
    }
    addSpace()
    withCurlyBraces {

        if (addKeyMapper) {
            addAssignKeyMapperIfNotValid()
        }

        addTextSegment("return")
        addSpace()
        addTextSegment(className)
        withParentheses {
            addNewLine()
            variables.forEach {
                addTextSegment(it.publicVariableName)
                addTextSegment(":")
                addSpace()

                addFromJsonVariableMapText(it, addKeyMapper)

                addTextSegment("== null ? null : ")

                addFromJsonTypeText(it, addKeyMapper)

                addComma()
                addNewLine()
            }
        }
        addSemicolon()
    }
}

private fun Template.addFromJsonTypeText(param: AliasedVariableTemplateParam, addKeyMapper: Boolean){
    val variableName = param.variableName
    val variableType = param.type

    if(TypeUtils.isCollection(variableType)){
        val collectionType = TypeUtils.getCollectionType(variableType)
        val childType = TypeUtils.getCollectionChildType(variableType)
        addTextSegment("$collectionType<$childType>.from(")
        addFromJsonVariableMapText(param, addKeyMapper)
        addTextSegment(".map((x)")
        addSpace()
        addTextSegment("=>")
        addSpace()
        if(TypeUtils.isPrimitive(childType)){
            addTextSegment("parse${childType.toUppercaseFirstChar()}(x)))")
        } else {
            addTextSegment("$childType.${TemplateConstants.FROM_JSON_METHOD_NAME}(x)))")
        }
    } else {
        if(TypeUtils.isPrimitive(variableType)){
            addTextSegment("parse${variableType.toUppercaseFirstChar()}(")
            addFromJsonVariableMapText(param, addKeyMapper)
            addTextSegment(")")
        } else {
            addTextSegment("$variableType.${TemplateConstants.FROM_JSON_METHOD_NAME}(")
            addFromJsonVariableMapText(param, addKeyMapper)
            addTextSegment(")")
        }
    }
}

private fun Template.addFromJsonVariableMapText(param: AliasedVariableTemplateParam, addKeyMapper: Boolean){
    addTextSegment(TemplateConstants.JSON_VARIABLE_NAME)

    withBrackets {
        "'${param.mapKeyString}'".also { keyParam ->
            if (addKeyMapper) {
                addTextSegment(TemplateConstants.KEYMAPPER_VARIABLE_NAME)
                withParentheses {
                    addTextSegment(keyParam)
                }
            } else {
                addTextSegment(keyParam)
            }
        }
    }
}
