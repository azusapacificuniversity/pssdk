package edu.apu.pssdk.example

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Kotlin ALL_CAPS getters (getEMPLID) mangle to "emplid"; take the JSON names off the fields.
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, isGetterVisibility = NONE)
@Schema(
    name = "AdvisingNoteRequest",
    description = "Request body shape. The endpoint copies these fields into the CI payload, applies defaults for " +
        "any missing field (INSTITUTION=APU, SAA_NOTE_ID=99999, SAA_NOTE_TYPE=ADVISING, SAA_NOTE_SUBTYPE=GENERAL, " +
        "SAA_NOTE_STATUS=OP), and synthesizes the SAA_ADV_NOTEDTL child rowset from SAA_ADV_NOTEDTL plus the " +
        "OPRID/DTTM fields."
)
data class AdvisingNoteRequest(
    @field:Schema(description = "PeopleSoft EMPLID of the student. CREATEKEY.", example = "12345678", requiredMode = REQUIRED)
    val EMPLID: String,

    @field:Schema(description = "CREATEKEY.", defaultValue = "99999")
    val SAA_NOTE_ID: String = "99999",

    @field:Schema(defaultValue = "ADVISING")
    val SAA_NOTE_TYPE: String = "ADVISING",

    @field:Schema(defaultValue = "GENERAL")
    val SAA_NOTE_SUBTYPE: String = "GENERAL",

    @field:Schema(description = "EMPLID of the advisor", example = "87654321")
    val ADVISOR_ID: String = "",

    @field:Schema(defaultValue = "OP", allowableValues = ["OP", "CL", "RE"])
    val SAA_NOTE_STATUS: String = "OP",

    @field:Schema(description = "Note subject line", example = "Spring registration", requiredMode = REQUIRED)
    val SAA_NOTE_SUBJ: String,

    @field:Schema(
        description = "Note body — becomes SAA_NOTE_ITM_LONG on the child rowset",
        example = "Met with student to plan Spring 2026 schedule.",
        requiredMode = REQUIRED
    )
    val SAA_ADV_NOTEDTL: String,

    @field:Schema(description = "Optional. Defaults to today.", format = "date")
    val SAA_NOTE_ADD_DT: String? = null,

    @field:Schema(description = "Advisor netid (operator id)", example = "advisor.netid")
    val SCC_ROW_ADD_OPRID: String? = null,

    @field:Schema(description = "Optional. Defaults to now.", format = "date-time")
    val SCC_ROW_ADD_DTTM: String? = null,
) {
    /** The full CI payload handed to `ci.save()`. Defaults match the express example. */
    fun toCiPayload(): Map<String, Any> {
        val advisorNetid = SCC_ROW_ADD_OPRID ?: ADVISOR_ID
        val noteDate = SAA_NOTE_ADD_DT ?: LocalDate.now().toString()
        val noteDttm = SCC_ROW_ADD_DTTM ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

        val noteDetail = listOf<Map<String, Any>>(
            mapOf(
                "SAA_ITEM_SEQ" to "1",
                "SAA_NOTE_ITM_DT" to noteDate,
                "SAA_NOTE_ITM_LONG" to SAA_ADV_NOTEDTL,
                "SCC_ROW_ADD_OPRID" to advisorNetid,
                "SCC_ROW_UPD_OPRID" to advisorNetid,
                "SCC_ROW_ADD_DTTM" to noteDttm,
            )
        )

        return mapOf(
            "EMPLID" to EMPLID,
            "INSTITUTION" to "APU",
            "SAA_NOTE_ID" to SAA_NOTE_ID,
            "SAA_NOTE_TYPE" to SAA_NOTE_TYPE,
            "SAA_NOTE_SUBTYPE" to SAA_NOTE_SUBTYPE,
            "ADVISOR_ID" to ADVISOR_ID,
            "SAA_NOTE_STATUS" to SAA_NOTE_STATUS,
            "SAA_NOTE_ACCESS" to "Y",
            "SAA_NOTE_CONTACT" to "",
            "SAA_NOTE_SUBJ" to SAA_NOTE_SUBJ,
            "SAA_NOTE_ADD_DT" to noteDate,
            "SAA_NOTE_UPD_DT" to noteDate,
            "SCC_ROW_ADD_OPRID" to advisorNetid,
            "SCC_ROW_UPD_OPRID" to advisorNetid,
            "SAA_ADV_NOTEDTL" to noteDetail,
        )
    }
}

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, isGetterVisibility = NONE)
@Schema(name = "AdvisingNoteResponse")
data class AdvisingNoteResponse(
    @field:Schema(example = "ok") val status: String,
    @field:Schema(example = "SAA_ADV_NOTE") val ci: String,
    @field:Schema(example = "99999") val SAA_NOTE_ID: String,
)

@Schema(name = "Error")
data class ApiError(
    @field:Schema(example = "ci_failure") val error: String,
    val message: String,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:Schema(description = "Java cause chain: PssdkException -> JOAException -> ...")
    val causes: List<Cause> = emptyList(),
) {
    data class Cause(val type: String, val message: String)
}
