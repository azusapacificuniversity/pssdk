<cfscript>
	cfcontent(type = "application/json", reset = true);
	cfheader(name = "Cache-Control", value = "no-store");
	cfheader(name = "Access-Control-Allow-Origin", value = "*");

	spec = {
		"openapi": "3.0.3",
		"info": {
			"title": "APU Advising Notes API",
			"version": "1.0.0",
			"description": "Lucee + [edu.apu.pssdk](https://github.com/azusapacificuniversity/pssdk) wrapper around the **SAA_ADV_NOTE** Component Interface.\n\n**Flow** (see `services/AdvisingNoteService.cfc`):\n\n1. `AppServer.fromEnv()` — picks up `PS_APPSERVER_HOSTPORT` / `_USERNAME` / `_PASSWORD` from the container env.\n2. `appServer.ciFactory(""SAA_ADV_NOTE"")` — opens the JOA session.\n3. `ci.create({EMPLID, INSTITUTION, SAA_NOTE_ID})` — populates the CREATEKEYS.\n4. `ci.save(fullNote)` — sets the rest of the fields plus the `SAA_ADV_NOTEDTL` child rowset.\n5. On any exception, `ci.cancel()` is invoked to release the JOA session."
		},
		"servers": [
			{ "url": "/", "description": "This Lucee instance" }
		],
		"tags": [
			{ "name": "Advising Notes", "description": "SAA_ADV_NOTE Component Interface" }
		],
		"paths": {
			"/api/v1/notes.cfm": {
				"post": {
					"tags": ["Advising Notes"],
					"summary": "Create + save an advising note",
					"description": "Calls `Create` + `Save` on the `SAA_ADV_NOTE` Component Interface. On failure, `Cancel` is invoked to release the JOA session.",
					"operationId": "createAdvisingNote",
					"requestBody": {
						"required": true,
						"content": {
							"application/json": {
								"schema": { "$ref": "##/components/schemas/AdvisingNoteRequest" },
								"examples": {
									"minimal": {
										"summary": "Minimal — let the server fill in defaults",
										"value": {
											"EMPLID": "12345678",
											"ADVISOR_ID": "87654321",
											"SAA_NOTE_SUBJ": "Spring registration",
											"SAA_ADV_NOTEDTL": "Met with student to plan Spring 2026 schedule.",
											"SCC_ROW_ADD_OPRID": "advisor.netid"
										}
									},
									"full": {
										"summary": "Full — every field explicit",
										"value": {
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
										}
									}
								}
							}
						}
					},
					"responses": {
						"201": {
							"description": "Note created and saved on the App Server",
							"content": {
								"application/json": {
									"schema": { "$ref": "##/components/schemas/AdvisingNoteResponse" }
								}
							}
						},
						"400": {
							"description": "Invalid JSON, empty body, or missing CREATEKEY",
							"content": {
								"application/json": {
									"schema": { "$ref": "##/components/schemas/Error" }
								}
							}
						},
						"405": {
							"description": "Wrong HTTP method (only POST is allowed)",
							"content": {
								"application/json": {
									"schema": { "$ref": "##/components/schemas/Error" }
								}
							}
						},
						"500": {
							"description": "PeopleSoft CI failure (Cancel was invoked)",
							"content": {
								"application/json": {
									"schema": { "$ref": "##/components/schemas/Error" }
								}
							}
						}
					}
				}
			}
		},
		"components": {
			"schemas": {
				"AdvisingNoteRequest": {
					"type": "object",
					"description": "Request body shape. The endpoint copies these fields into the CI payload, applies defaults for any missing field (INSTITUTION=APU, SAA_NOTE_ID=99999, SAA_NOTE_TYPE=ADVISING, SAA_NOTE_SUBTYPE=GENERAL, SAA_NOTE_STATUS=OP), and synthesizes the `SAA_ADV_NOTEDTL` child rowset from `SAA_ADV_NOTEDTL` + the OPRID/DTTM fields.",
					"required": ["EMPLID", "SAA_NOTE_SUBJ", "SAA_ADV_NOTEDTL"],
					"properties": {
						"EMPLID":            { "type": "string", "description": "PeopleSoft EMPLID of the student. CREATEKEY.", "example": "12345678" },
						"SAA_NOTE_ID":       { "type": "string", "description": "CREATEKEY. Defaults to 99999 if omitted.", "default": "99999" },
						"SAA_NOTE_TYPE":     { "type": "string", "default": "ADVISING" },
						"SAA_NOTE_SUBTYPE":  { "type": "string", "default": "GENERAL" },
						"ADVISOR_ID":        { "type": "string", "description": "EMPLID of the advisor", "example": "87654321" },
						"SAA_NOTE_STATUS":   { "type": "string", "default": "OP", "enum": ["OP", "CL", "RE"] },
						"SAA_NOTE_SUBJ":     { "type": "string", "description": "Note subject line", "example": "Spring registration" },
						"SAA_ADV_NOTEDTL":   { "type": "string", "description": "Note body — becomes SAA_NOTE_ITM_LONG on the child rowset", "example": "Met with student to plan Spring 2026 schedule." },
						"SAA_NOTE_ADD_DT":   { "type": "string", "format": "date", "description": "Optional. Defaults to today." },
						"SCC_ROW_ADD_OPRID": { "type": "string", "description": "Advisor netid (operator id)", "example": "advisor.netid" },
						"SCC_ROW_ADD_DTTM":  { "type": "string", "format": "date-time", "description": "Optional. Defaults to now()." }
					}
				},
				"AdvisingNoteCIPayload": {
					"type": "object",
					"description": "Full CI payload that the server constructs and hands to `ci.save()`. Documented for reference — clients do **not** post this shape directly.",
					"properties": {
						"EMPLID":            { "type": "string" },
						"INSTITUTION":       { "type": "string", "example": "APU" },
						"SAA_NOTE_ID":       { "type": "string", "example": "99999" },
						"SAA_NOTE_TYPE":     { "type": "string", "example": "ADVISING" },
						"SAA_NOTE_SUBTYPE":  { "type": "string", "example": "GENERAL" },
						"ADVISOR_ID":        { "type": "string" },
						"SAA_NOTE_STATUS":   { "type": "string", "example": "OP" },
						"SAA_NOTE_ACCESS":   { "type": "string", "example": "Y" },
						"SAA_NOTE_CONTACT":  { "type": "string" },
						"SAA_NOTE_SUBJ":     { "type": "string" },
						"SAA_NOTE_ADD_DT":   { "type": "string", "format": "date" },
						"SAA_NOTE_UPD_DT":   { "type": "string", "format": "date" },
						"SCC_ROW_ADD_OPRID": { "type": "string" },
						"SCC_ROW_UPD_OPRID": { "type": "string" },
						"SAA_ADV_NOTEDTL": {
							"type": "array",
							"items": { "$ref": "##/components/schemas/AdvisingNoteDetailRow" }
						}
					}
				},
				"AdvisingNoteDetailRow": {
					"type": "object",
					"description": "Child rowset row on the SAA_ADV_NOTEDTL scroll.",
					"properties": {
						"SAA_ITEM_SEQ":      { "type": "string", "example": "1" },
						"SAA_NOTE_ITM_DT":   { "type": "string", "format": "date" },
						"SAA_NOTE_ITM_LONG": { "type": "string" },
						"SCC_ROW_ADD_OPRID": { "type": "string" },
						"SCC_ROW_UPD_OPRID": { "type": "string" },
						"SCC_ROW_ADD_DTTM":  { "type": "string", "format": "date-time" }
					}
				},
				"AdvisingNoteResponse": {
					"type": "object",
					"properties": {
						"status":      { "type": "string", "example": "ok" },
						"ci":          { "type": "string", "example": "SAA_ADV_NOTE" },
						"SAA_NOTE_ID": { "type": "string", "example": "99999" }
					}
				},
				"Error": {
					"type": "object",
					"properties": {
						"error":   { "type": "string", "example": "ci_failure" },
						"message": { "type": "string" },
						"detail":  { "type": "string" }
					}
				}
			}
		}
	};

	writeOutput(serializeJSON(spec));
</cfscript>
