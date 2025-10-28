package com.studyhub.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study")
public class Study {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(length = 50)
  private String category;

  @Column(nullable = false)
  private Integer capacity = 10;

  @Column(nullable = false, length = 10)
  private String status = "OPEN"; // OPEN / CLOSED

  @Column(name = "image_url", length = 255)
  private String imageUrl;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "apply_deadline")
  private LocalDateTime applyDeadline;

  // --- getter/setter (필요한 것만) ---
  public Long getId() { return id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public Integer getCapacity() { return capacity; }
  public void setCapacity(Integer capacity) { this.capacity = capacity; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getImageUrl() { return imageUrl; }
  public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public LocalDateTime getApplyDeadline() { return applyDeadline; }
  public void setApplyDeadline(LocalDateTime applyDeadline) { this.applyDeadline = applyDeadline; }
}
