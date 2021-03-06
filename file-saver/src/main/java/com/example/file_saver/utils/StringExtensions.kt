package com.example.file_saver.utils

fun String?.nullIfEmpty(): String? {
    return this?.takeUnless { it.isEmpty() }
}

fun String.suffixOrEmpty(): String {
    return substringAfterLast(DOT, EMPTY_STRING)
}

fun String.suffixOrNull(): String? {
    return suffixOrEmpty().nullIfEmpty()
}