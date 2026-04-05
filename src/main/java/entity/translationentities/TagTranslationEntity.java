package entity.translationentities;

import entity.base.BaseTranslationEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="tag_translation")
public class TagTranslationEntity extends BaseTranslationEntity {
}
