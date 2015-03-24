package ntu.nlp.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ntu.nlp.component.Comment;
import ntu.nlp.component.TermWrapper;
import ntu.nlp.controller.ParallelClientMain.CallableTasker;
import ntu.nlp.service.InputParser;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.cheyingwu.ckip.Term;

public class TestParallel {

	static{
		PropertyConfigurator.configure("conf/log4j.properties");
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(TestParallel.class);
	private static final String LOC = "input_files/207884_hotel_training.txt";
	private static final String IP = "140.109.19.104";
	private static final int PORT = 1501;
	private static final int MAX_ROUND = 4000;
	private static final String ACCOUNT = "0708lwy";
	private static final String PWD = "0708lwy";
	
	public static void main(String[] args){
		
		LOG.info( " =============================Start Test=============================== " );
		List<Comment> commentList = InputParser.readFile(LOC);
		ConcurrentLinkedQueue<Comment> q = new ConcurrentLinkedQueue<Comment>(commentList);
		
		Map<Integer, Future<TermWrapper>> passResultMap = new TreeMap<Integer, Future<TermWrapper>>();
		List<String> lines = new ArrayList<String>(); 
				
		final int THREAD_NUM = Runtime.getRuntime().availableProcessors();
		ExecutorService es = Executors.newFixedThreadPool(THREAD_NUM);
		int counter = 0;
		
		while(counter < MAX_ROUND  &&  !q.isEmpty()){
			Comment comment = q.poll();
			
			CallableTasker caller = new CallableTasker(comment);
			Future<TermWrapper> future = es.submit(caller);
			try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e) {
				LOG.error("Thread InterruptedException!");
				e.printStackTrace();
			}
			if(future.isDone()){
				try {
					if(future.get() != null){
						LOG.info("[Finish serial number] :" + future.get().getSerial());
						passResultMap.put(future.get().getSerial(), future);
					}else{
						q.add(comment);		
					}
				} catch (InterruptedException e) {
					LOG.error("Thread Pool InterruptedException!");
					e.printStackTrace();
				} catch (ExecutionException e) {
					LOG.error("Thread Pool ExecutionException!");
					e.printStackTrace();
				}
				counter++;	
			}
		
		}
		
        es.shutdown();
        try {
            es.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("Thread Pool InterruptedException after shutdown!");
        }
		        
        if(es.isShutdown()){
        	        	
        	for(Integer key : passResultMap.keySet()){
        		try {
        			Future<TermWrapper> future = passResultMap.get(key);
        			TermWrapper tw = future.get();
					LOG.info("Success case serial: " + tw.getSerial());
					for(Term term: tw.getTerm()){
							lines.add(tw.getSerial() +","+ tw.getRank() + "," + term.getTerm() + "," + term.getTag());
					}
				} catch (InterruptedException e) {
					LOG.error("Thread Pool InterruptedException!");
				} catch (ExecutionException e) {
					LOG.error("Thread Pool ExecutionException!");
				} 
        	}

        	try {
                    File file = new File("output/"+ UUID.randomUUID().toString() + ".csv");
    				FileUtils.writeLines(file, lines, Boolean.TRUE);
			} catch (IOException e) {
				LOG.error("writing file task fails!");
			}			
        	
        	while(!q.isEmpty()){
                LOG.error("Send Fail! Serial ID: " + q.poll().getSerial());
        	}
        	
        }// End of es-conditioner
        
	}
	
}
