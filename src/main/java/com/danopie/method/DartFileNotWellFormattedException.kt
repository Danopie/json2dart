package com.danopie.method

import java.lang.RuntimeException

class DartFileNotWellFormattedException(
    message: String? = null
) : RuntimeException(message)