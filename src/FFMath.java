
public class FFMath {

	/**
	 * Calculates the max value in a double array (I could generalize it, but not necessary)
	 * @param Double array
	 * @return max value in the array
	 */
	public static double max(double[] arr){
		double max = Double.MIN_VALUE;
		for (double d : arr){
			if (d > max){
				max = d;
			}
		}
		return max;
	}

	public static double max(Double[] arr) {
		double max = Double.MIN_VALUE;
		for (double d : arr){
			if (d > max){
				max = d;
			}
		}
		return max;
	}
	
	/**
	 * Calculates the min value in a double array
	 * @param Double array
	 * @return min value in the array
	 */
	public static double min(double[] arr){
		double min = Double.MAX_VALUE;
		for (double d : arr){
			if (d < min){
				min = d;
			}
		}
		return min;
	}
	
	public static double min(Double[] arr){
		double min = Double.MAX_VALUE;
		for (double d : arr){
			if (d < min){
				min = d;
			}
		}
		return min;
	}
	
	public static void main(String[] args){
		System.out.println(FFMath.min(new double[]{1d,2d,3d,3.01d,2d, 1d}));
		
	}
}
