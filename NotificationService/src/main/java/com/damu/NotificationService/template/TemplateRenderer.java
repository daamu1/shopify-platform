package com.damu.NotificationService.template;

import java.util.Map;

public interface TemplateRenderer {

    String render(String template, Map<String, Object> data);
}
