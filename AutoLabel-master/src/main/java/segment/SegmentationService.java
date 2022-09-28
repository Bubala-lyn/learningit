package segment;

import java.util.ArrayList;

public interface SegmentationService {
	
	int maxLength=10000;

	String[] segment(ArrayList<ArrayList<String>> words) throws Exception;
	
	String segment(String text);
}
