package com.aiworkspace.documents.infrastructure.validation;

import com.aiworkspace.documents.application.port.out.MimeTypeValidatorPort;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

@Component
public class TikaMimeTypeValidatorAdapter implements MimeTypeValidatorPort {

    private final Tika tika = new Tika();

    @Override
    public String detect(byte[] content) {
        return tika.detect(content);
    }
}
