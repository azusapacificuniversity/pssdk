package edu.apu.pssdk.example

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun invalidJson(e: HttpMessageNotReadableException) =
        ResponseEntity.badRequest().body(ApiError("invalid_json", e.mostSpecificCause.message ?: ""))

    @ExceptionHandler(MissingCreateKeyException::class)
    fun missingCreateKey(e: MissingCreateKeyException) =
        ResponseEntity.badRequest().body(ApiError("missing_create_key", e.message ?: ""))

    /** PssdkException chains the underlying JOAException; walk it so the real reason is visible. */
    @ExceptionHandler(Exception::class)
    fun ciFailure(e: Exception): ResponseEntity<ApiError> {
        // Standard MVC failures (405, 415, ...) carry their own status.
        if (e is ErrorResponse) {
            return ResponseEntity.status(e.statusCode).body(ApiError("request_error", e.message ?: ""))
        }
        val causes = generateSequence<Throwable>(e) { it.cause }
            .map { ApiError.Cause(it.javaClass.name, it.message ?: "") }
            .toList()
        log.error("CI failure: {}", causes, e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError("ci_failure", e.message ?: "", causes))
    }
}
