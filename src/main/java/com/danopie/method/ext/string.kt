package com.danopie.method.ext

fun String.toUppercaseFirstChar() : String{
    return "${this.first().toUpperCase()}${this.substring(1)}"
}