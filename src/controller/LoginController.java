
package controller;

import java.io.IOException;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import dao.LoginDao;

public class LoginController {

    @FXML
    private Button btnlogin;

    @FXML
    private Label password;

    @FXML
    private TextField txtpassword;

    @FXML
    private TextField txtusername;

    @FXML
    private Label username;

    @FXML
    void loginClick(ActionEvent event) throws SQLException, IOException {
    	String name = txtusername.getText();
    	String psw = txtpassword.getText();
    	boolean status = LoginDao.check_credential(name, psw);
    	if (status) {
    		username.setText("successful");
    		Stage stage = (Stage) btnlogin.getScene().getWindow();
			stage.close();
			Parent root = FXMLLoader.load(getClass().getResource("../fxmldesign/MainView.fxml"));
			Scene scence = new Scene(root, 1200, 800);
			stage.setScene(scence);
			stage.setMaximized(true);
			stage.show();
    	}
    	else {
    		 Alert alert = new Alert(Alert.AlertType.WARNING);
    	        alert.setTitle("Login Error");
    	        alert.setHeaderText(null);
    	        alert.setContentText("Wrong UserName Or Password");
    	        alert.showAndWait();
    	}
    }

}

