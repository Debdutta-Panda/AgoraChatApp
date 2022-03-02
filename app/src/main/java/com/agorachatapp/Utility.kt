package com.agorachatapp

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*


val guid: String
get(){
    return UUID.randomUUID().toString()
}

val utcTimestamp: Long
get(){
    return DateTime(DateTimeZone.UTC).millis
}