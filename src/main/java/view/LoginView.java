package view;

import javafx.application.Application;
import javafx.stage.Stage;
import util.NavigationUtil;

public class LoginView extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        NavigationUtil.navigateTo(stage, "/FXML/login_view.fxml", "Login window");

        stage.setResizable(false);

        stage.show();
    }
}
