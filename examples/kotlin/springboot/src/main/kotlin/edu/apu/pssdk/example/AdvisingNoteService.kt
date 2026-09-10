package edu.apu.pssdk.example

import edu.apu.pssdk.AppServer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

class MissingCreateKeyException(message: String) : RuntimeException(message)

/**
 * Thin Kotlin wrapper around edu.apu.pssdk for the SAA_ADV_NOTE Component Interface.
 *
 * Mirrors the JS example pattern: AppServer.fromEnv() -> ciFactory(name) -> create / save / cancel.
 * Kotlin maps and lists are java.util.Map / java.util.List, so the JSON payload flows straight into
 * CI.create / CI.save without manual marshalling.
 */
@Service
class AdvisingNoteService {

    private val log = LoggerFactory.getLogger(javaClass)

    // Lazy so the app starts (and Swagger UI works) without App Server credentials in the environment.
    private val appServer: AppServer by lazy { AppServer.fromEnv() }

    /** Create + Save an advising note. On any failure, Cancel is invoked before the session is closed. */
    fun createAdvisingNote(note: Map<String, Any>): AdvisingNoteResponse {
        val createKeys = CREATE_KEYS.associateWith { key ->
            val value = note[key]
            if (value == null || value.toString().isBlank()) {
                throw MissingCreateKeyException("CREATEKEY '$key' is required for $CI_NAME")
            }
            value
        }

        log.info("Saving advising note: {}", note)
        // CI is Closeable, and cancel() only invokes CANCEL -- it does not release the session,
        // so close() has to run on both paths.
        appServer.ciFactory(CI_NAME).use { ci ->
            try {
                ci.create(createKeys)
                ci.save(note)
                // PeopleSoft auto-numbers SAA_NOTE_ID on Save when the CREATEKEY is the 99999 placeholder,
                // so read the key back off the CI. toProxyObject() is the SDK's way out of a saved CI.
                val savedNoteId = ci.toProxyObject().getMember(NOTE_ID)?.toString() ?: note[NOTE_ID].toString()
                log.info("Saved advising note {}={}", NOTE_ID, savedNoteId)
                return AdvisingNoteResponse(status = "ok", ci = CI_NAME, SAA_NOTE_ID = savedNoteId)
            } catch (e: Exception) {
                runCatching { ci.cancel() }
                    .onFailure { log.warn("Cancel failed; the original error wins", it) }
                throw e
            }
        }
    }

    companion object {
        const val CI_NAME = "SAA_ADV_NOTE"
        const val NOTE_ID = "SAA_NOTE_ID"

        // ci.create() takes only the CREATEKEYS; the rest of the data (including the
        // SAA_ADV_NOTEDTL child rowset) goes through ci.save().
        val CREATE_KEYS = listOf("EMPLID", "INSTITUTION", NOTE_ID)
    }
}
