package ntu.nlp.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IODealer {

	private static final Logger LOG = LoggerFactory.getLogger(IODealer.class);
	
	public static void writeFile(List<String> lines, String filetype) throws IOException{
		
            File file = new File("output/"+ UUID.randomUUID().toString() + filetype);
			FileUtils.writeLines(file, "UTF-8", lines, Boolean.TRUE);
			
	}
	
}
