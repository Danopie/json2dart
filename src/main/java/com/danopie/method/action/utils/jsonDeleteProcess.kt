package com.danopie.method.action.utils

import com.danopie.method.templater.TemplateConstants
import com.danopie.method.utils.mergeCalls
import com.jetbrains.lang.dart.psi.DartClass

fun createJsonDeleteCall(
    dartClass: DartClass
): (() -> Unit)? {

    val toJsonMethod = dartClass.findMethodByName(TemplateConstants.TO_JSON_METHOD_NAME)
    val fromJsonMethod = dartClass.findNamedConstructor(TemplateConstants.FROM_JSON_METHOD_NAME)

    return listOfNotNull(
        toJsonMethod,
        fromJsonMethod
    )
        .map { { it.delete() } }
        .mergeCalls()
}