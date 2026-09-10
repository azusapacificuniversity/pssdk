/**
 * Thin CFML wrapper around edu.apu.pssdk for the SAA_ADV_NOTE Component Interface.
 *
 * Mirrors the JS example pattern: AppServer.fromEnv() -> ciFactory(name) -> create / save / cancel.
 * Lucee auto-converts CFML structs to java.util.Map and arrays to java.util.List when invoking
 * Java methods, so the JSON payload from the request body can be passed straight through.
 */
component accessors=false {

	variables.CI_NAME = "SAA_ADV_NOTE";

	// CREATEKEYS for the SAA_NOTE record. ci.create() expects only the create keys; the rest of
	// the data (including the SAA_ADV_NOTEDTL child rowset) goes through ci.save().
	variables.CREATE_KEYS = ["EMPLID", "INSTITUTION", "SAA_NOTE_ID"];

	function init() {
		variables.AppServerClass = createObject("java", "edu.apu.pssdk.AppServer");
		return this;
	}

	/**
	 * Create + Save an advising note. On any failure, Cancel is invoked before the session is closed.
	 *
	 * @note Struct shaped like the documented advising-note payload (EMPLID, INSTITUTION,
	 *       SAA_NOTE_ID, SAA_NOTE_TYPE, ..., SAA_ADV_NOTEDTL[]).
	 * @return Struct with status + the SAA_NOTE_ID that was written.
	 */
	struct function createAdvisingNote(required struct note) {
		var appServer = variables.AppServerClass.fromEnv();
		var ci = appServer.ciFactory(variables.CI_NAME);

		try {
			var createKeys = {};
			for (var key in variables.CREATE_KEYS) {
				if (!structKeyExists(arguments.note, key)) {
					throw(
						type = "MissingCreateKey",
						message = "CREATEKEY '#key#' is required for #variables.CI_NAME#"
					);
				}
				createKeys[key] = arguments.note[key];
			}

			ci.create(createKeys);
			ci.save(arguments.note);

			return {
				"status": "ok",
				"ci": variables.CI_NAME,
				"SAA_NOTE_ID": arguments.note.SAA_NOTE_ID
			};
		} catch (any e) {
			try {
				ci.cancel();
			} catch (any ignored) {
				// Cancel can fail if the session is already torn down; the original error wins.
			}
			rethrow;
		} finally {
			// cancel() only invokes CANCEL; close() is what releases the JOA session, so it has
			// to run on both the success and the failure path.
			try {
				ci.close();
			} catch (any ignored) {
				// Already disconnected.
			}
		}
	}
}
