package containers;


public class GraphStatus {

	
	private int runNumber;
	private FFError status;
	
	public GraphStatus(int number, FFError status){
		this.runNumber = number;
		this.status = status;
	}
	
	public int getNumber(){
		return this.runNumber;
	}
	
	public FFError getStatus(){
		return this.status;
	}
	
	public String toString(){
		return "<GraphStatus>:\nNumber: "+this.runNumber+"\nStatus: "+this.status+"\n";
	}
}
