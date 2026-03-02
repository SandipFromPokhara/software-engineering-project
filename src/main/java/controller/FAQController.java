package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import util.WindowUtil;

public class FAQController {

    @FXML
    private Button closeBtn;

    @FXML
    private Accordion faqAccordion;

    public void initFaq(boolean isGuest) {
        faqAccordion.getPanes().clear(); // Reset content

        if (isGuest) {
            addPane("What is NoteVault?","NoteVault is a secure, hierarchical note-taking application. It allows you to organize your thoughts into notebooks and individual notes, all structured within a clean, layered architecture.");
            addPane("Is NoteVault free to use?", "Yes! NoteVault is completely free for all users. We believe in providing a distraction-free environment for your thoughts.");
            addPane("Do I need an internet connection to use NoteVault?", "No. NoteVault is a desktop application designed for offline use. Your notes are stored locally on your machine, ensuring you can access your work anytime, anywhere.");
            addPane("Is my data secure?", "Yes. We prioritize your privacy. All user passwords are encrypted before storage. Because the database is local, your notes never leave your device unless you choose to export them.");
            addPane("Where is my data stored?", "Your data is stored locally on your machine in a secure MariaDB database. It never leaves your computer unless you explicitly choose to export or back it up.");
            addPane("Why should I register an account?", "Registering unlocks full persistence. You will be able to create unlimited notebooks, save individual notes, use tags for organization, and export your work to PDF.");
            addPane("Can I save my notes as a Guest?", "As a guest, you can freely write and experiment, but your notes will not be saved. To preserve your work, please register an account.");
        } else {
            addPane("How do I manage my work?", "You have full Create, Read, Edit and Delete control. You can create multiple notebooks, organize notes within them, and use tags to cross-reference your ideas.");
            addPane("How does the tagging system work?", "Tags are keywords that help you categorize notes. Simply type your tag in the combo box and click \"Add tags.\" You can then view notes by these tags in the Dashboard.");
            addPane("How do I manage tags?", "You can add or remove tags in the Create or Edit Note window.");
            addPane("How do I format my text?", "Use the toolbar above the editor area. You can increase or decrease font-sizes and create bulleted or numbered list. Changes are applied to whole text.");
            addPane("How do I export my notes?", "In the editor, use the \"Export\" button to convert your notes into PDF format, making it easy to share your work or print it.");
            addPane("Can I change the look of the app?", "Yes! You can toggle between Dark and Light themes via the settings menu in the Dashboard to suit your environment.");
            addPane("What do the word/character counters mean?", "The status bar shows real-time stats for the note you are currently writing or editing, perfect for keeping track of essay lengths or quick memos.");
            addPane("Is my data automatically backed up?", "Since NoteVault is a local application, it does not automatically sync to the cloud. We highly recommend occasionally copying your database file to an external drive or cloud service for safety.");
            addPane("How do I manage my account?", "Navigate to the \"Account Settings\" section in the Dashboard to update your profile or manage your session.");
        }
    }

    private void addPane(String title, String contentText) {
        Label contentLabel = new Label(contentText);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.setStyle("-fx-padding: 10; -fx-text-fill: #333333;");

        TitledPane pane = new TitledPane(title, contentLabel);
        faqAccordion.getPanes().add(pane);
    }

    @FXML
    private void handleClose() {
        WindowUtil.closeWindow(faqAccordion);
    }
}
