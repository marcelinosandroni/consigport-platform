package br.com.consigport.adapters.in.web.dto;

/**
 * Detail of an error of validation, by field.
 *
 * @param field the field or the path related to the error
 * @param message the localized explanation of the problem
 */
public record ErrorDetail(String field, String message) {}
