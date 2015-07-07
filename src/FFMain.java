import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class FFMain extends Application {

	public static Parent root;
	public static Stage stage;
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		// TODO Auto-generated method stub
		
		Font.loadFont(FFMain.class.getResourceAsStream("expressway.ttf"), 12);
		stage = primaryStage;
		root = FXMLLoader.load(getClass().getResource("FFLayout.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("FitzFitting SPROX Analysis v1.0");
		stage.setScene(scene);
		stage.show();
	}
	
	public void restart(){
		stage.close();
		try{
			start(new Stage());
		}
		catch (IOException e){
			e.printStackTrace();
			exit();
		}
	}
	
	public void exit(){
		Platform.exit();
		System.exit(0);
	}
	
	
	//Backup
	public static void main(String[] args){
		launch(args);
	}
}
