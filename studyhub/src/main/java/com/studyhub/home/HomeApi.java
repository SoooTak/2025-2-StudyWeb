package com.studyhub.home;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeApi {
  @GetMapping("/api/home/summary")
  public Map<String, Object> summary() {
    return Map.of(
      "calendarEvents", List.of(
        Map.of("title","1주차 모임","start","2025-11-01T19:00:00"),
        Map.of("title","2주차 모임","start","2025-11-08T19:00:00")
      ),
      "promos", Map.of(
        "latest", List.of(
          Map.of("id",100,"title","알고리즘 스터디","category","CS","status","OPEN")
        ),
        "closingSoon", List.of()
      ),
      "myStudies", List.of()
    );
  }
}
