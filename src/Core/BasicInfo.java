package Core;
public class BasicInfo {
	private static final int processorNum = Runtime.getRuntime().availableProcessors();
    public BasicInfo() {
    	System.out.println("Get basic information");
    }
    
    public int getProcessors() {
    	return processorNum;
    }
    
    public static void main(String[] args) {
    	BasicInfo basicInfo = new BasicInfo();
    	System.out.println("the number of the available processors is " + basicInfo.getProcessors());
    }
}
