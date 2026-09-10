component {
	this.name = "pssdk-example-lucee";
	this.sessionManagement = false;
	this.applicationTimeout = createTimeSpan(0, 1, 0, 0);

	// Preserve struct key case in serializeJSON output. Without this, Lucee upper-cases keys,
	// which breaks the OpenAPI version detection in Swagger UI ("openapi" -> "OPENAPI").
	this.serialization = { preserveCaseForStructKey: true };

	function onApplicationStart() {
		application.advisingNoteService = new services.AdvisingNoteService();
		return true;
	}

	function onRequestStart(required string targetPage) {
		if (structKeyExists(url, "reload")) {
			applicationStop();
			location(url = cgi.script_name, addToken = false);
		}
		return true;
	}

	function onError(required any exception, required string eventName) {
		if (findNoCase("/api/", cgi.script_name)) {
			cfheader(statustext = "Internal Server Error", statuscode = "500");
			cfcontent(type = "application/json");
			writeOutput(serializeJSON({
				"error": "internal_error",
				"message": arguments.exception.message ?: "",
				"detail": arguments.exception.detail ?: ""
			}));
			abort;
		}
		throw(object = arguments.exception);
	}
}
