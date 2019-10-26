package ext.test;

public class Name {

	   public static void main(String[] args) {
		 
		   String name = "";
		   String count = name.replaceAll("\\d+","");
	    	
	       String[] arry = count.split("\\|");
	    	
	       System.out.println(arry[0]);
	   }
	 
}
