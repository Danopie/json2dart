package com.danopie.json.delegates.generator.data

import com.danopie.json.delegates.generator.toClassName
import com.danopie.json.delegates.generator.toSneakCase
import com.fasterxml.jackson.databind.JsonNode

data class NodeWrapper(
        val node: JsonNode?,
        val fieldName: String,
        val sneakCaseName: String = toSneakCase(fieldName),
        val className: String = toClassName(fieldName)
)