package containers;

public class CSVStringBuilder{
	StringBuilder sb;
	public CSVStringBuilder(){
		sb = new StringBuilder();
	}
	public void append(String s){
		sb.append(s);
		sb.append(",");
	}
	public void append(int i){
		sb.append(i);
		sb.append(",");
	}
	public void append(double d){
		sb.append(d);
		sb.append(",");
	}
	public void append(double[] darr){
		for(int i = 0; i < darr.length; i++){
			this.append(darr[i]);
		}
	}
	public void addEmptyCell(){
		this.append(",");
	}
	
	public String toString(){
		StringBuilder ret = new StringBuilder(sb);
		return ret.substring(0, ret.length()-1).toString()+"\n";
	}
}