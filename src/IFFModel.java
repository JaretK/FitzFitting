import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.text.Text;



public interface IFFModel {
	
	public abstract void start();
	
	public abstract void writeLoadedMessage();
	
	public abstract void save();
	
	public abstract void generateGraphs();
	
	public abstract void terminate();
	
	public FFError getStatus();
	
	public void writeError(String text);
	
	public boolean getGenerateGraphsStatus();
	
	public SimpleBooleanProperty runningProperty();
	
	public SimpleDoubleProperty progressProperty();
}
