package statics;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class TextFlowWriter {

	/**
	 * Colors used throughout for styling purposes
	 */
	
	public static final Color FFBlue = Color.web("#235F9C");
	public static final Color FFGreen = Color.GREEN;
	public static final Color FFRed = Color.RED;
	
	/**
	 * TEXTFLOW METHODS
	 */
	
	public static void clear(TextFlow tf){
		Platform.runLater(()->{
			tf.getChildren().clear();
		});
		//Shift TextFlow down a line
		TextFlowWriter.writeLine("", tf);
	}
	
	public static void removeLast(TextFlow tf){
		Platform.runLater(()->{
			tf.getChildren().remove(tf.getChildren().size()-1);
		});
	}

	public static void writeLine(String line, TextFlow tf){
		Text text = new Text(line+"\n");
		addText(text, tf);
	}
	public static void appendText(String text, TextFlow tf){
		addText(new Text(text), tf);
	}
	public static void writeInfo(String infoText, TextFlow tf){
		Text text = new Text(infoText+"\n");
		text.setFill(FFBlue);
		addText(text, tf);
	}
	public static synchronized void writeSuccess(String successText, TextFlow tf){
		Text text = new Text(successText+"\n");
		text.setFill(FFGreen);
		addText(text, tf);
	}
	public static void writeError(String errorText, TextFlow tf){
		Text text = new Text(errorText+"\n");
		text.setFill(FFRed);
		text.setStyle("-fx-stroke:Black");
		text.setStyle("-fx-stroke-width:0.1");
		addText(text, tf);
	}
	
	public static void addArray(Text[] textLines, TextFlow tf){
		for (Text ele : textLines){
			addText(ele, tf);
		}
	}
	
	public static synchronized void addText(Text text, TextFlow tf){
		Platform.runLater(()-> {
			tf.getChildren().add(text);
		});
	}
	
}
