package com.danopie.method.utils

object TypeUtils {
    fun isPrimitive(type: String): Boolean {
        return when(type.trim()){
            "int" -> true
            "double" -> true
            "String" -> true
            "bool" -> true
            "dynamic" -> true
            "DateTime" -> true
            else -> false
        }
    }

    fun isCollection(type: String): Boolean {
        return type.trim().startsWith("List<") && type.endsWith(">") ||
                type.trim().startsWith("Map<") && type.endsWith(">")
    }

    fun getCollectionType(type: String): String{
        return type.trim().substringBefore("<")
    }

    fun getCollectionChildType(type: String): String{
        return type.trim().substringAfter("${getCollectionType(type)}<").substringBeforeLast(">")
    }
}