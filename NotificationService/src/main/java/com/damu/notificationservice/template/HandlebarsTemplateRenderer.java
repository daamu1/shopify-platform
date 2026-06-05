package com.damu.notificationservice.template;

import com.github.jknack.handlebars.Handlebars;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

@Component
public class HandlebarsTemplateRenderer implements TemplateRenderer {

    private final Handlebars handlebars = new Handlebars();

    @Override
    public String render(String template, Map<String, Object> data) {
        if (template == null || template.isBlank()) {
            return "";
        }
        try {
            return handlebars.compileInline(template).apply(data == null ? Map.of() : data);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
