<cfscript>
	cfheader(name = "Cache-Control", value = "no-store");
	cfcontent(type = "application/json");

	if (cgi.request_method != "POST") {
		cfheader(statustext = "Method Not Allowed", statuscode = "405");
		writeOutput(serializeJSON({ "error": "method_not_allowed", "allow": "POST" }));
		abort;
	}

	rawBody = toString(getHttpRequestData().content);
	if (!len(trim(rawBody))) {
		cfheader(statustext = "Bad Request", statuscode = "400");
		writeOutput(serializeJSON({ "error": "empty_body" }));
		abort;
	}

	try {
		body = deserializeJSON(rawBody);
	} catch (any e) {
		cfheader(statustext = "Bad Request", statuscode = "400");
		writeOutput(serializeJSON({ "error": "invalid_json", "message": e.message }));
		abort;
	}

	// Build the CI payload exactly as documented. Defaults match the express example.
	advisorEmplid = body.ADVISOR_ID ?: "";
	advisorNetid  = body.SCC_ROW_ADD_OPRID ?: advisorEmplid;
	dtNote        = body.SAA_NOTE_ADD_DT ?: dateTimeFormat(now(), "yyyy-mm-dd");
	dttmNote      = body.SCC_ROW_ADD_DTTM ?: dateTimeFormat(now(), "yyyy-mm-dd'T'HH:nn:ss");

	notedtl = [{
		"SAA_ITEM_SEQ"      : "1",
		"SAA_NOTE_ITM_DT"   : dtNote,
		"SAA_NOTE_ITM_LONG" : body.SAA_ADV_NOTEDTL ?: "",
		"SCC_ROW_ADD_OPRID" : advisorNetid,
		"SCC_ROW_UPD_OPRID" : advisorNetid,
		"SCC_ROW_ADD_DTTM"  : dttmNote
	}];

	note = {
		"EMPLID"            : body.EMPLID ?: "",
		"INSTITUTION"       : "APU",
		"SAA_NOTE_ID"       : body.SAA_NOTE_ID ?: "99999",
		"SAA_NOTE_TYPE"     : body.SAA_NOTE_TYPE ?: "ADVISING",
		"SAA_NOTE_SUBTYPE"  : body.SAA_NOTE_SUBTYPE ?: "GENERAL",
		"ADVISOR_ID"        : advisorEmplid,
		"SAA_NOTE_STATUS"   : body.SAA_NOTE_STATUS ?: "OP",
		"SAA_NOTE_ACCESS"   : "Y",
		"SAA_NOTE_CONTACT"  : "",
		"SAA_NOTE_SUBJ"     : body.SAA_NOTE_SUBJ ?: "",
		"SAA_NOTE_ADD_DT"   : dtNote,
		"SAA_NOTE_UPD_DT"   : dtNote,
		"SCC_ROW_ADD_OPRID" : advisorNetid,
		"SCC_ROW_UPD_OPRID" : advisorNetid,
		"SAA_ADV_NOTEDTL"   : notedtl
	};

	try {
		systemOutput("Saving advising note: " & serializeJSON(note), true);
		result = application.advisingNoteService.createAdvisingNote(note);
		cfheader(statustext = "Created", statuscode = "201");
		writeOutput(serializeJSON(result));
	} catch (MissingCreateKey e) {
		cfheader(statustext = "Bad Request", statuscode = "400");
		writeOutput(serializeJSON({ "error": "missing_create_key", "message": e.message }));
	} catch (any e) {
		// Walk the Java cause chain so PssdkException -> JOAException -> ... is visible.
		// CFML's `e.detail` is empty for raw Java exceptions; the real reason lives on .cause.
		causes = [];
		cur = e;
		while (!isNull(cur)) {
			causes.append({
				"type":    cur.type    ?: (cur.getClass().getName() ?: ""),
				"message": cur.message ?: ""
			});
			cur = cur.cause ?: javacast("null", "");
		}
		systemOutput("CI failure: " & serializeJSON(causes), true);
		cfheader(statustext = "Internal Server Error", statuscode = "500");
		writeOutput(serializeJSON({
			"error": "ci_failure",
			"message": e.message ?: "",
			"causes": causes
		}));
	}
</cfscript>
