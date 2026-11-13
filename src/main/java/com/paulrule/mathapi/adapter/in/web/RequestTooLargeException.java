package com.paulrule.mathapi.adapter.in.web;

import java.util.Locale;

/** The request body is over {@link RequestBodyLimit#MAX_CHARACTERS} characters (PRD §3.2). */
class RequestTooLargeException extends RuntimeException {

    RequestTooLargeException() {
        super(String.format(
                Locale.ROOT, "The request body must be at most %,d characters.", RequestBodyLimit.MAX_CHARACTERS));
    }
}
