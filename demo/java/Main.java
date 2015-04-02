import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;


public class Main extends Application {

    // Called from Persona JS.
    public void AuthCallback(String assertion) {
        System.out.println("ASSERTION");
        System.out.println(assertion);

        // TODO: verify assertion. Probably post this back to folktune.org for verification, get back a token.
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Log in to FolkTune.org");
        stage.setWidth(500);
        stage.setHeight(400);

        Scene scene = new Scene(new Group());
        VBox root = new VBox();    
        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();


        // TODO: automatically trigger when web view has loaded.
        Button loginButton = new Button("Login");
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                webEngine.executeScript("BrowserID.internal.get('folktune.org', function(assertion) { container.AuthCallback(assertion) });");
            }
        });

        webEngine.load("https://login.persona.org/sign_in#NATIVE");
        
        root.getChildren().addAll(loginButton, browser);

        JSObject window = (JSObject) webEngine.executeScript("window");
        window.setMember("container", this);

        scene.setRoot(root);
 
        stage.setScene(scene);
        stage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}