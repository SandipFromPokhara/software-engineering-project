package entity.base;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@MappedSuperclass
public class BaseTranslationEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="language_code", nullable = false, length = 6)
    private String langCode;

    @Column(name="createdAt")
    private LocalDateTime createdAt;

    @Column(name="updatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getLangCode() { return langCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setLangCode(String langCode) {
        if (this.langCode != null) {
            throw new IllegalStateException("Language code is immutable");
        }
        this.langCode = langCode.toUpperCase(); }
}