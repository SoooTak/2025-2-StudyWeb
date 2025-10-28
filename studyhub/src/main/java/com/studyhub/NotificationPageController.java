package com.studyhub;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationPageController {

  @GetMapping("/notifications")
  public String page() {
    return "notifications"; // src/main/resources/templates/notifications.html
  }
}
