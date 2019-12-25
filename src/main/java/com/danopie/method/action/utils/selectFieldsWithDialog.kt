package com.danopie.method.action.utils

import com.danopie.method.declaration.DeclarationExtractor
import com.danopie.method.declaration.VariableDeclaration
import com.danopie.method.dialog.GenerateDialog
import com.intellij.openapi.project.Project
import com.jetbrains.lang.dart.psi.DartClassDefinition

/**
 * Returns null if user decided to cancel the operation
 */
fun selectFieldsWithDialog(
    project: Project,
    dartClass: DartClassDefinition
): List<VariableDeclaration>? {

    val declarations = DeclarationExtractor.extractDeclarationsFromClass(dartClass)

    val dialog = GenerateDialog(
        project,
        declarations
    )
    dialog.show()

    if (!dialog.isOK) {
        return null
    }

    return dialog.getSelectedFields()
}