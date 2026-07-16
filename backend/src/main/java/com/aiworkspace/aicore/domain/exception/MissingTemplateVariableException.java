package com.aiworkspace.aicore.domain.exception;

import com.aiworkspace.shared.exception.DomainException;

import java.util.Set;

public class MissingTemplateVariableException extends DomainException {

    public MissingTemplateVariableException(String templateName, Set<String> missing) {
        super("Template '" + templateName + "' is missing required variables: " + missing);
    }
}
