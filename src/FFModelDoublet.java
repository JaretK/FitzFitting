import javafx.scene.text.TextFlow;



/**
 * Same as FFModelSinglet, but this class allows for comparison between two data sets
 * 
 * This is called when Compare IS selected
 * @author jkarnuta
 *
 */


public class FFModelDoublet extends AbstractFFModel{
	
	private final String SPROX2;

	public FFModelDoublet(String SPROX1,String SPROX2 ,String denaturantPath, TextFlow tf,
			boolean generateGraphs, double midpoint) {
		super(SPROX1, denaturantPath, tf, generateGraphs, midpoint);
		this.SPROX2 = SPROX2;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeLoadedMessage() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateGraphs() {
		// TODO Auto-generated method stub
		
	}
	
}