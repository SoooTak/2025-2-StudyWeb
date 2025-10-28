package com.studyhub;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SessionPageController {
  @GetMapping("/sessions")
  public String page() { return "sessions"; }
}
