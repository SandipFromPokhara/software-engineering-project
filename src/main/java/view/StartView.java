package view;

import javafx.application.Application;
import javafx.stage.Stage;
import util.NavigationUtil;

<<<<<<<< HEAD:src/main/java/view/EntryView.java
public class EntryView extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        NavigationUtil.navigateTo(stage, "/FXML/entry.fxml", "NoteVault - Welcome");
========
public class StartView extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        NavigationUtil.navigateTo(stage, "/FXML/login_view.fxml", "Welcome to NoteVault", false);
>>>>>>>> 79a8e34f059d9cb94d2fa303d57541199fa3bc21:src/main/java/view/StartView.java
    }
}
