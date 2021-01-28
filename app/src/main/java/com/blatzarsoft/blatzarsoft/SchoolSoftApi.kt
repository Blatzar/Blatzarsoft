package com.blatzarsoft.blatzarsoft

import com.blatzarsoft.blatzarsoft.DataStore.mapper
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import khttp.get

data class Lunch(
    @JsonProperty("week") val week: Int,
    @JsonProperty("dates") val dates: List<String>,

    @JsonProperty("monday") val monday: String,
    @JsonProperty("tuesday") val tuesday: String,
    @JsonProperty("wednesday") val wednesday: String,
    @JsonProperty("thursday") val thursday: String,
    @JsonProperty("friday") val friday: String,
    // Not used
    @JsonProperty("saturday") val saturday: String,
    @JsonProperty("sunday") val sunday: String
)

data class Lesson(
    @JsonProperty("weeksString") val weeksString: String,
    @JsonProperty("subjectName") val subjectName: String,
    @JsonProperty("roomName") val roomName: String,
    @JsonProperty("length") val length: Int,
    @JsonProperty("startTime") val startTime: String,
    @JsonProperty("endTime") val endTime: String,
    @JsonProperty("dayId") val dayId: Int,
    @JsonProperty("id") val id: Int
)

data class Orgs(@JsonProperty("orgId") val orgId: Int)
data class Person(
    @JsonProperty("name") val name: String,
    @JsonProperty("appKey") val appKey: String,
    @JsonProperty("orgs") val orgs: List<Orgs>
)

data class Token(@JsonProperty("expiryDate") val expiryDate: String, @JsonProperty("token") val token: String)

class SchoolSoftApi {
    companion object {

        fun getToken(school: String, appKey: String): Token? {
            val url = "https://sms.schoolsoft.se/${school}/rest/app/token"
            val payload = mapOf(
                "appversion" to "2.3.2",
                "appos" to "android",
                "appkey" to appKey,
                "deviceid" to ""
            )

            return try {
                val r = get(url, headers = payload)
                if (r.statusCode == 200) {
                    mapper.readValue<Token>(r.text)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        fun getAppKey(school: String, name: String, password: String): Any? {
            // Example login return
            // """{"pictureUrl":"pictureFile.jsp?studentId=9000","name":"LagradOst","isOfAge":true,"appKey":"string",
            // "orgs":[
            //          {"name":"string","blogger":false,"schoolType":9,"leisureSchool":0,"class":"string","orgId":int,"tokenLogin":"url"}
            // ],
            // "type":1,"userId":9000}"""
            val url = "https://sms.schoolsoft.se/${school}/rest/app/login"

            val payload = mapOf(
                "identification" to name,
                "verification" to password,
                "logintype" to "4",
                "usertype" to "1"
            )
            try {
                val r = get(url, data = payload)
                if (r.statusCode != 200) {
                    return r.statusCode
                }
                return mapper.readValue<Person>(r.text)
            } catch (ex: Exception) {
                return null
            }
        }


        fun getLunch(school: String, token: String, orgId: Int): List<Lunch>? {
            val url = "https://sms.schoolsoft.se/${school}/api/lunchmenus/student/${orgId}"
            val payload = mapOf(
                "appversion" to "2.3.2",
                "appos" to "android",
                "token" to token
            )
            val r = get(url, headers = payload)
            return if (r.statusCode == 200) {
                mapper.readValue<List<Lunch>>(r.text)
            } else {
                null
            }
        }

        fun getLessons(school: String, token: String, orgId: Int): List<Lesson>? {
            val url = "https://sms.schoolsoft.se/${school}/api/lessons/student/${orgId}"
            val payload = mapOf(
                "appversion" to "2.3.2",
                "appos" to "android",
                "token" to token
            )
            val r = get(url, headers = payload)
            return if (r.statusCode == 200) {
                mapper.readValue<List<Lesson>>(r.text)
            } else {
                null
            }
        }


        fun weekStringToList(weeks: String): MutableList<Int> {
            // Converts "2, 5-8" to [2, 5, 6, 7, 8]
            val fullList = mutableListOf<Int>()
            val splitList = weeks.split(",")
            splitList.forEach {
                val splitNumbers = it.split("-")
                val first = splitNumbers[0].trim().toIntOrNull()

                if (splitNumbers.size == 1) {
                    if (first != null) {
                        fullList.add(first)
                    }
                } else {
                    val second = splitNumbers[1].trim().toIntOrNull()
                    if (first != null && second != null) {
                        for (i in first..second) {
                            fullList.add(i)
                        }
                    }
                }
            }
            return fullList
        }
    }
}