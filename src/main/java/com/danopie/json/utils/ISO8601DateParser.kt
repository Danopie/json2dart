package com.danopie.json.utils

/*
 * Copyright 1999,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.text.DateFormat
import java.text.ParseException
import java.time.format.DateTimeParseException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.temporal.TemporalAccessor






/**
 * ISO 8601 date parsing utility.  Designed for parsing the ISO subset used in
 * Dublin Core, RSS 1.0, and Atom.
 *
 * @author [Kevin A. Burton (burtonator)](mailto:burton@apache.org)
 * @version $Id: ISO8601DateParser.java,v 1.2 2005/06/03 20:25:29 snoopdave Exp $
 */
object ISO8601DateValidator {

    fun isValid(dateStr: String): Boolean {
        return try {
            Instant.parse(dateStr)
            true
        } catch (e: DateTimeParseException){
            false
        }
    }

}