package com.paulrule.mathapi.adapter.in.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * Bounds every request body to 16,384 characters before Jackson sees it (PRD §3.2). Jackson's own document limit
 * counts bytes, and the contract counts characters, so the body is read here, at most 4 bytes per character.
 */
@ControllerAdvice
class RequestBodyLimit extends RequestBodyAdviceAdapter {

    static final int MAX_CHARACTERS = 16_384;

    private static final int MAX_UTF8_BYTES = 4 * MAX_CHARACTERS;

    @Override
    public boolean supports(
            MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(
            HttpInputMessage message,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType)
            throws IOException {
        byte[] body = message.getBody().readNBytes(MAX_UTF8_BYTES + 1);
        if (body.length > MAX_UTF8_BYTES || new String(body, StandardCharsets.UTF_8).length() > MAX_CHARACTERS) {
            throw new RequestTooLargeException();
        }
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(body);
            }

            @Override
            public HttpHeaders getHeaders() {
                return message.getHeaders();
            }
        };
    }
}
