package com.damu.notificationservice.template;

import java.util.Map;

public interface TemplateRenderer {

    String render(String template, Map<String, Object> data);
}
