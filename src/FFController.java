import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;



public class FFController extends FFMain implements Initializable{

	@FXML
	private Button SPROXBrowse, SPROX2Browse, DenaturantBrowse, AnalyzeButton;

	@FXML
	private TextField SPROXField, SPROX2Field ,DenaturantField;

	@FXML
	private CheckBox Urea, Graphs, CompareInputs;

	@FXML
	private Group SPROX2Group;
	
	@FXML
	private MenuItem Reset, Exit;

	@FXML
	private TextFlow FFInfo;

	@FXML
	private ScrollPane FFInfoContainer;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		initTextFields();

		/*
		 * Do not allow indeterminate states for CheckBoxes
		 */
		Urea.setAllowIndeterminate(false);
		Graphs.setAllowIndeterminate(false);
		
		/**
		 * Add listeners
		 */
		
		/*
		 * Force FFInfo to scroll down on each append
		 */
		FFInfo.getChildren().addListener(
				(ListChangeListener<Node>) ((change) -> {
					FFInfo.layout();
					FFInfoContainer.layout();
					FFInfoContainer.setVvalue(1.0f);
				}));
		
		CompareInputs.selectedProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				SPROX2Group.setDisable(!newValue);
			}
		});
		
		/*
		 * Write Greeting
		 */
		writeLine("");
		String name = System.getProperty("user.name");
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		String version = System.getProperty("os.version");
		writeInfo("Hello "+name);
		writeInfo("I've detected you're running "  +os+" ("+version+", "+arch+")");

	}

	/*
	 * Exits the javaFX thread
	 * Called from Exit
	 */
	public void ExitOnAction(){
		super.exit();
	}
	/*
	 * Closes the stage
	 * Called from Reset
	 */
	public void ResetOnAction(){
		super.restart();
	}

	/**
	 * TEXT FIELD METHODS
	 */

	private void initTextFields(){
		initSPROXField();
		initSPROX2Field();
		initDenaturantsField();
	}

	private void initSPROXField(){
		setupDragOver(SPROXField);
		setupDragDrop(SPROXField);
	}
	
	private void initSPROX2Field(){
		setupDragOver(SPROX2Field);
		setupDragDrop(SPROX2Field);
	}

	private void initDenaturantsField(){
		setupDragOver(DenaturantField);
		setupDragDrop(DenaturantField);
	}

	private void setupDragOver(TextField tf){
		tf.setOnDragOver(new EventHandler<DragEvent>(){
			@Override
			public void handle(DragEvent event) {
				if (event.getGestureSource() != tf &&
						event.getDragboard().hasFiles()){
					event.acceptTransferModes(TransferMode.LINK);
				}
				event.consume();
			}
		});
	}

	private void setupDragDrop(TextField tf){
		tf.setOnDragDropped(new EventHandler<DragEvent>(){
			@Override
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				boolean success = false;
				if (db.hasFiles() && db.getFiles().size() == 1){
					tf.setText(db.getFiles().get(0).getAbsolutePath());
					success = true;
				}
				event.setDropCompleted(success);
				event.consume();
			}
		});
	}

	public void SPROXOnDragEntered(){
		TextFieldOnDragEntered(SPROXField);
	}

	public void SPROXOnDragExited(){
		TextFieldOnDragExited(SPROXField);
	}
	
	public void SPROX2OnDragEntered(){
		TextFieldOnDragEntered(SPROX2Field);
	}
	
	public void SPROX2OnDragExited(){
		TextFieldOnDragExited(SPROX2Field);
	}

	public void DenaturantOnDragEntered(){
		TextFieldOnDragEntered(DenaturantField);
	}

	public void DenaturantOnDragExited(){
		TextFieldOnDragExited(DenaturantField);
	}

	private void TextFieldOnDragEntered(TextField tf){
		tf.setStyle("-fx-border-color:#235F9C;-fx-border-style:solid;");
	}
	private void TextFieldOnDragExited(TextField tf){
		tf.setStyle("-fx-border-style:none;-fx-border-color: transparent;");
	}

	/**
	 * TEXTFIELD BUTTON METHODS
	 */
	public void SPROXButtonOnAction(){
		fetchFile(SPROXField, "Select SPROX csv");
	}
	
	public void SPROX2ButtonOnAction(){
		fetchFile(SPROX2Field, "Select SPROX csv for Comparison");
	}

	public void DenaturantButtonOnAction(){
		fetchFile(DenaturantField, "Select Denaturants File");
	}
	private void fetchFile(TextField tf, String title){
		Stage fcStage = new Stage();
		fcStage.initModality(Modality.APPLICATION_MODAL);
		fcStage.initOwner(stage.getScene().getWindow());

		FileChooser fc = new FileChooser();
		fc.setTitle(title);
		fc.setInitialDirectory(
				new File(System.getProperty("user.dir"))
				);
		File chosenFile = fc.showOpenDialog(fcStage);

		if(chosenFile != null){
			tf.setText(chosenFile.getAbsolutePath());
		}

	}

	/**
	 * TEXTFLOW METHODS
	 */

	private void writeLine(String line){
		Text text = new Text(line+"\n");
		addText(text);
	}
	private void appendText(String text){
		addText(new Text(text));
	}
	private void writeInfo(String infoText){
		Text text = new Text(infoText+"\n");
		text.setFill(Color.web("#235F9C"));
		addText(text);
	}
	private void writeSuccess(String successText){
		Text text = new Text(successText+"\n");
		text.setFill(Color.CHARTREUSE);
		addText(text);
	}
	private void writeError(String errorText){
		Text text = new Text(errorText+"\n");
		text.setFill(Color.RED);
		text.setStyle("-fx-stroke:Black");
		text.setStyle("-fx-stroke-width:0.1");
		addText(text);
	}
	private synchronized void addText(Text text){
		Platform.runLater(()-> {
			FFInfo.getChildren().add(text);
		});
	}
	/**
	 * Analyze On Action
	 */
	public void AnalyzeOnAction(){
		double midpointdiff = Urea.isSelected() ? 1.0 : 0.5;
		
	}

}
