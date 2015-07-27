import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import statics.*;
import models.*;
import datasets.*;
public class FFController extends FFMain implements Initializable{

	private static final boolean DEBUG = false;

	@FXML
	private Button SPROXBrowse, SPROX2Browse, DenaturantBrowse, AnalyzeButton;

	@FXML
	private TextField SPROXField, SPROX2Field ,DenaturantField, 
	MidPointValue, AdjustedRSquaredValue, DifferenceValue;

	@FXML
	private CheckBox Graphs, CompareInputs, Dual;

	@FXML
	private Group SPROXGroup, SPROX2Group;

	@FXML
	private MenuItem Reset, Exit;

	@FXML
	private TextFlow FFInfo;

	@FXML
	private ScrollPane FFInfoContainer;

	@FXML
	private ProgressBar progressBar;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		initTextFields();

		/*
		 * Do not allow indeterminate states for CheckBoxes
		 */
		Graphs.setAllowIndeterminate(false);

		/**
		 * Add listeners and bindings
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
		FFInfo.setPrefWidth(Region.USE_COMPUTED_SIZE);

		/*
		 * Disable 2nd sprox field if CompareInputs not selected
		 */
		CompareInputs.selectedProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				SPROX2Group.setDisable(!newValue);
				SPROX2Field.setText("");
				Dual.setDisable(newValue);
				if(newValue == true){//false -> true
					Dual.setSelected(!newValue);
					
				}
			}
		});

		/*
		 * Disable analyze button if nothing in 1st Sprox / Denaturants
		 */
		AnalyzeButton.disableProperty().bind(Bindings.or(SPROXGroup.disabledProperty(),Bindings.or(SPROXField.textProperty().isEqualTo(""), 
				Bindings.and(CompareInputs.selectedProperty(), SPROX2Field.textProperty().isEqualTo("")))));

		/*
		 * Only allow Dual or CompareInputs to be selected, not both
		 */

		Dual.selectedProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldVal, Boolean newVal){
				CompareInputs.setDisable(newVal);
				if(newVal == true){ // false -> true
					CompareInputs.setSelected(!newVal);
				}
			}
		});

		/*
		 * Write Greeting
		 */
		TextFlowWriter.writeLine("", FFInfo);
		String name = System.getProperty("user.name");
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		String version = System.getProperty("os.version");
		TextFlowWriter.writeInfo("Hello "+name, FFInfo);
		TextFlowWriter.writeInfo("I've detected you're running "  +os+" ("+version+", "+arch+")", FFInfo);


		if(DEBUG){
			SPROXField.setText("/Users/jkarnuta/Desktop/10-16-12 manA Control Data.csv");
			DenaturantField.setText("/Users/jkarnuta/Desktop/manATags.csv");
			AnalyzeButton.fire();
		}

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
	 * Analyze On Action
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void AnalyzeOnAction(){

		/*
		 * Clear TextFlow
		 */

		TextFlowWriter.clear(FFInfo);

		/*
		 * Build constants for constructors
		 */
		boolean genGraphs = Graphs.isSelected();
		String SPROX1 = SPROXField.getText();
		String SPROX2 = SPROX2Field.getText();
		String denaturants = DenaturantField.getText();
		
		/*
		 * Set values for heuristics 
		 */
		FFConstants.setAdjustedRSquaredHeuristic(AdjustedRSquaredValue.getText());
		FFConstants.setMidPointHeuristic(MidPointValue.getText());
		FFConstants.setDifferenceHeuristic(DifferenceValue.getText());

		AbstractFFModel model;
		AbstractDataSet dataset;
		//Compare two files of type Singlets
		if (CompareInputs.isSelected()){
			System.err.println("Not Implemented");
			TextFlowWriter.writeError("FFModelDoublet is not yet implemented", FFInfo);
			model = new FFModelDoublet(SPROX1, SPROX2,denaturants, FFInfo, genGraphs);
			dataset = new DoubletDataSet(SPROX1, denaturants, FFInfo);
			dataset.addDoubletNature(SPROX2);
		}
		
		//Compare two experiments in one file of type Doublet
		else if (Dual.isSelected()){
			model = new FFModelDualSinglet(SPROX1, denaturants, FFInfo, genGraphs);
			dataset = new DualSingletDataSet(SPROX1, denaturants, FFInfo);
		}
		
		//Default option
		else{
			model = new FFModelSinglet(SPROX1, denaturants, FFInfo, genGraphs);
			dataset = new SingletDataSet(SPROX1, denaturants, FFInfo);
		}
		/*
		 * Add bindings for communication between model and controller
		 */
		SPROXGroup.setDisable(true);
		model.runningProperty().addListener(new ChangeListener() {
			@Override
			public void changed(ObservableValue observable, Object oldValue,
					Object newValue) {
				SPROXGroup.setDisable((boolean) newValue);
			}
		});
		
		progressBar.progressProperty().bind(model.progressProperty());
		model.load(dataset);
		super.loadAndStart(model);
	}
	/**
	 * Getter Methods
	 */

	public ProgressBar getProgressBar(){
		return progressBar;
	}

}
