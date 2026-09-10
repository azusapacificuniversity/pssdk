package edu.apu.pssdk.example

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notes", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Advising Notes", description = "SAA_ADV_NOTE Component Interface")
class AdvisingNoteController(private val service: AdvisingNoteService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create + save an advising note",
        description = "Calls `Create` + `Save` on the `SAA_ADV_NOTE` Component Interface. " +
            "On failure, `Cancel` is invoked to release the JOA session.",
        operationId = "createAdvisingNote",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                schema = Schema(implementation = AdvisingNoteRequest::class),
                examples = [
                    ExampleObject(
                        name = "minimal",
                        summary = "Minimal — let the server fill in defaults",
                        value = """{
  "EMPLID": "12345678",
  "ADVISOR_ID": "87654321",
  "SAA_NOTE_SUBJ": "Spring registration",
  "SAA_ADV_NOTEDTL": "Met with student to plan Spring 2026 schedule.",
  "SCC_ROW_ADD_OPRID": "advisor.netid"
}"""
                    ),
                    ExampleObject(
                        name = "full",
                        summary = "Full — every field explicit",
                        value = """{
  "EMPLID": "12345678",
  "SAA_NOTE_ID": "99999",
  "SAA_NOTE_TYPE": "ADVISING",
  "SAA_NOTE_SUBTYPE": "GENERAL",
  "ADVISOR_ID": "87654321",
  "SAA_NOTE_STATUS": "OP",
  "SAA_NOTE_SUBJ": "Spring registration",
  "SAA_ADV_NOTEDTL": "Met with student to plan Spring 2026 schedule.",
  "SAA_NOTE_ADD_DT": "2026-05-08",
  "SCC_ROW_ADD_OPRID": "advisor.netid",
  "SCC_ROW_ADD_DTTM": "2026-05-08T10:30:00"
}"""
                    ),
                ]
            )]
        )
    )
    @ApiResponse(responseCode = "201", description = "Note created and saved on the App Server")
    @ApiResponse(
        responseCode = "400",
        description = "Invalid JSON or missing CREATEKEY",
        content = [Content(schema = Schema(implementation = ApiError::class))]
    )
    @ApiResponse(
        responseCode = "500",
        description = "PeopleSoft CI failure (Cancel was invoked)",
        content = [Content(schema = Schema(implementation = ApiError::class))]
    )
    fun createNote(@RequestBody request: AdvisingNoteRequest): AdvisingNoteResponse =
        service.createAdvisingNote(request.toCiPayload())
}
