package com.danopie.json.delegates.generator

import com.danopie.json.delegates.generator.data.NodeInfo
import com.danopie.json.delegates.generator.data.NodeWrapper
import com.danopie.json.utils.ISO8601DateValidator
import com.danopie.method.ext.toUppercaseFirstChar
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.*
import java.util.*


class DartClassGenerator {

    fun generateFromJson(source: String, destiny: File, rootName: String, isFinal: Boolean) {
        val nodesToProcessStack = Stack<NodeWrapper>()

        try {
            nodesToProcessStack.add(
                    NodeWrapper(
                            node = jacksonObjectMapper().readTree(source),
                            fieldName = rootName,
                            sneakCaseName = rootName,
                            className = extractRootClassName(rootName)
                    )
            )
        } catch (e: Exception) {
            throw SyntaxException()
        }

        val packageTemplate = extractPackageName(destiny)
        val finalMode = if (isFinal) "final " else ""
        var nodeWrapper: NodeWrapper
        var nodeInfo: NodeInfo
        var buffer: FileOutputStream
        var target: FileOutputStream
        var constructorStringBuilder: StringBuilder
        var serializatorStringBuilder: StringBuilder
        val importsList = mutableListOf<String>()
        var bufferFile: File

        while (nodesToProcessStack.isNotEmpty()) {
            nodeWrapper = nodesToProcessStack.pop()
            bufferFile = File(destiny, "__${nodeWrapper.sneakCaseName}.dart")
            buffer = FileOutputStream(bufferFile)
            target = FileOutputStream(File(destiny, "${nodeWrapper.sneakCaseName}.dart"))
            constructorStringBuilder = createConstructorStart(nodeWrapper)
            serializatorStringBuilder = createSerializatorStart()

            target.writeText("import 'package:primitive_type_parser/primitive_type_parser.dart';\n")

            buffer.writeText("\nclass ${nodeWrapper.className} {\n\n")
            try {
                nodeWrapper.node?.fields()?.forEach {
                    nodeInfo = processNode(buffer, it.value, it.key, finalMode)
                    nodeInfo.node?.apply {
                        nodesToProcessStack.add(this)
                        target.writeText("import '$packageTemplate$sneakCaseName.dart';\n")
                    }
                    serializatorStringBuilder.append(nodeInfo.mapSerialization)
                    constructorStringBuilder.append("\t\t${it.key} = ${nodeInfo.mapDeserialization}")
                }

                constructorStringBuilder.apply {
                    deleteCharAt(length - 1).deleteCharAt(length - 1).append(";\n")
                }
                serializatorStringBuilder
                    .append("\t\treturn map;\n")
                    .append("\t}\n")

                buffer.writeText(constructorStringBuilder.toString()).writeText("\n")
                buffer.writeText(serializatorStringBuilder.toString())
                buffer.writeText("}")
                buffer.close()

                mergeBufferAndTarget(target, bufferFile)
            } finally {
                buffer.close()
                target.close()
            }
            importsList.clear()
        }
    }

    private fun processNode(
        fout: FileOutputStream, node: JsonNode, name: String, finalMode: String
    ): NodeInfo {
        val nodeInfo = extractNodeInfo(node, name)
        fout.writeText("  $finalMode${nodeInfo.stringRepresentation} $name;\n")
        return nodeInfo
    }

    private fun extractNodeInfo(node: JsonNode, name: String): NodeInfo {
        return when {
            node.isDouble || node.isFloat || node.isBigDecimal ->
                NodeInfo("double", name, getParseFunction(name, "double"))

            node.isShort || node.isInt || node.isLong || node.isBigInteger ->
                NodeInfo("int", name, getParseFunction(name, "int"))

            node.isBoolean ->
                NodeInfo("bool", name, getParseFunction(name, "bool"))

            node.isTextual -> {
                if(ISO8601DateValidator.isValid(node.textValue())){
                    NodeInfo("DateTime", name, getParseFunction(name, "DateTime"), "\t\tmap['$name'] = $name?.toIso8601String();\n")
                } else {
                    NodeInfo("String", name, getParseFunction(name, "String"))
                }
            }
            node.isArray ->
                extractArrayData(node as ArrayNode, name)

            node.isObject ->
                NodeWrapper(node, name).toObjectNodeInfo()

            else -> NodeInfo("Object", name)
        }
    }

    private fun getParseFunction(name: String, className: String) = "parse${className.toUppercaseFirstChar()}(map[\"$name\"]),\n"

    private fun extractArrayData(node: ArrayNode, name: String): NodeInfo {
        val iterator = node.iterator()
        if (!iterator.hasNext()) {
            return NodeInfo("List<Object>", name)
        }
        val elementInfo = extractNodeInfo(iterator.next(), name)
        return NodeInfo(
                "List<${elementInfo.stringRepresentation}>",
                elementInfo.node,
                elementInfo.buildListDeserialization(name),
                elementInfo.buildListSerialization(name)
        )
    }

    private fun createConstructorStart(nodeWrapper: NodeWrapper) =
        StringBuilder()
            .append("\n\t${nodeWrapper.className}.fromJson(Map<String, dynamic> map): \n")


    private fun createSerializatorStart() =
        StringBuilder()
            .append("\tMap<String, dynamic> toJson() {\n")
            .append("\t\tfinal map = Map<String, dynamic>();\n")

    private fun mergeBufferAndTarget(targetStream: FileOutputStream, bufferFile: File) {
        BufferedReader(FileReader(bufferFile)).useLines { lines ->
            lines.forEach {
                targetStream.writeText(it).writeText("\n")
            }
        }

        bufferFile.delete()
    }

    private fun extractPackageName(dir: File): String {
        val absolutePath = dir.absolutePath
        val splitted = absolutePath.split(if (isWindows()) "\\" else "/")
        val libIndex = splitted.indexOf("lib")
        if (libIndex == -1) {
            throw NotAFlutterProject()
        }
        val fold = splitted
            .subList(libIndex + 1, splitted.size)
            .fold(StringBuilder()) { builder, s -> builder.append(s).append("/") }
        return "package:${splitted[libIndex - 1]}/$fold"
    }

    private fun FileOutputStream.writeText(text: String): FileOutputStream {
        write(text.toByteArray(Charsets.UTF_8))
        return this
    }

    private fun NodeWrapper.toObjectNodeInfo(): NodeInfo {
        val field = this.fieldName
        return NodeInfo(
                className,
                this,
                "map[\"$fieldName\"] == null ? null :  $className.fromJson(map[\"$fieldName\"]),\n",
                "\t\tmap['$field'] = $field == null ? null : $field.toJson();\n"
        )
    }

    private fun NodeInfo.buildListDeserialization(rawName: String) =
        if (node != null) {
            "map[\"${node.fieldName}\"] == null? null : List<${node.className}>.from(map[\"${node.fieldName}\"]" +
                ".map((it) => ${node.className}.fromJson(it))),\n"
        } else {
            "map[\"$rawName\"] == null? null : List<$stringRepresentation>.from(map[\"$rawName\"]),\n"
        }

    private fun NodeInfo.buildListSerialization(rawName: String) =
        if (node != null) {
            "\t\tmap['$rawName'] = ${node.fieldName} != null ? \n" +
                "\t\t\tthis.${node.fieldName}.map((v) => v.toJson()).toList()\n" +
                "\t\t\t: null;\n"
        } else {
            "\t\tmap['$rawName'] = $rawName;\n"
        }


    private fun extractRootClassName(rootFileName: String): String {
        var needUp = true
        val builder = StringBuilder()
        val i = rootFileName.iterator()
        var element: Char

        while (i.hasNext()) {
            element = i.nextChar()
            if (element == '_') {
                needUp = true
                continue
            }
            if (needUp) {
                element = element.toUpperCase()
                needUp = false
            }

            builder.append(element)
        }
        return builder.toString()
    }
}