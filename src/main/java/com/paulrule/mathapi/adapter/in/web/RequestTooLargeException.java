package com.paulrule.mathapi.adapter.in.web;

/** The request body is over {@link RequestBodyLimit#MAX_CHARACTERS} characters (PRD §3.2). */
class RequestTooLargeException extends RuntimeException {

    RequestTooLargeException() {
        super("The request body must be at most 16,384 characters.");
    }
}
