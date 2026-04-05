package entity.translationentities;

import entity.base.BaseTranslationEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="notebook_translation")
public class NotebookTranslationEntity extends BaseTranslationEntity {
}
