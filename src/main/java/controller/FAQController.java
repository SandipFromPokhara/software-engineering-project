package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import util.Localization;
import util.WindowUtil;

public class FAQController {

    @FXML
    private Button closeBtn;

    @FXML
    private Accordion faqAccordion;

    @FXML
    public void initialize() {
        closeBtn.textProperty().bind(Localization.bind("faq.close"));
    }

    public void initFaq(boolean isGuest) {
        faqAccordion.getPanes().clear(); // Reset content

        if (isGuest) {
            // Get Questions and answer form resource bundle
            addPane(Localization.get("faq.q1"),Localization.get("faq.a1"));
            addPane(Localization.get("faq.q2"),Localization.get("faq.a2"));
            addPane(Localization.get("faq.q3"),Localization.get("faq.a3"));
            addPane(Localization.get("faq.q4"),Localization.get("faq.a4"));
            addPane(Localization.get("faq.q5"),Localization.get("faq.a5"));
            addPane(Localization.get("faq.q6"),Localization.get("faq.a6"));
            addPane(Localization.get("faq.q7"),Localization.get("faq.a7"));
        } else {
            addPane(Localization.get("faq.q8"),Localization.get("faq.a8"));
            addPane(Localization.get("faq.q9"),Localization.get("faq.a9"));
            addPane(Localization.get("faq.q10"),Localization.get("faq.a10"));
            addPane(Localization.get("faq.q11"),Localization.get("faq.a11"));
            addPane(Localization.get("faq.q12"),Localization.get("faq.a12"));
            addPane(Localization.get("faq.q13"),Localization.get("faq.a13"));
            addPane(Localization.get("faq.q14"),Localization.get("faq.a14"));
            addPane(Localization.get("faq.q15"),Localization.get("faq.a15"));
            addPane(Localization.get("faq.q16"),Localization.get("faq.a16"));
        }
    }

    private void addPane(String title, String contentText) {
        Label contentLabel = new Label(contentText);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);

        contentLabel.getStyleClass().add("faq-answer-label");
        contentLabel.setStyle("-fx-padding: 10;");

        TitledPane pane = new TitledPane(title, contentLabel);
        faqAccordion.getPanes().add(pane);
    }

    @FXML
    private void handleClose() {
        WindowUtil.closeWindow(faqAccordion);
    }
}
