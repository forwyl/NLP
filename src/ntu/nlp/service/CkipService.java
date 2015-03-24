package ntu.nlp.service;

import java.util.ArrayList;
import java.util.List;

import ntu.nlp.component.Comment;
import ntu.nlp.component.TermWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.cheyingwu.ckip.CKIP;
import tw.cheyingwu.ckip.Term;
import tw.cheyingwu.ckip.WordSegmentationService;

public class CkipService {

	private static final Logger LOG = LoggerFactory.getLogger(CkipService.class);


	 public List<List<Term>> ckipSplitTagByList(String ip, int port, String account, String pwd, List<Comment> list){
		
		List<List<Term>> termList = new ArrayList<List<Term>>();
		
		WordSegmentationService service = new CKIP(ip, port, account, pwd);
		LOG.debug("*****�}�l�ϥΤ���|�_�����A�� *****");	
		for(Comment comment: list){
			service.setRawText(comment.getMsg());
			service.send();					
			LOG.debug("��l��ơG" + service.getRawText());
			termList.add(service.getTerm());
		}
		
		return termList;
		
	}

	
	 public TermWrapper ckipSplitTagByComment(String ip, int port, String account, String pwd, Comment comment){
			
		WordSegmentationService service = new CKIP(ip, port, account, pwd);
		LOG.debug("*****�}�l�ϥΤ���|�_�����A�� *****");	

		service.setRawText(comment.getMsg());
		service.send();					
		LOG.debug("���o��l��ơG" + service.getRawText());
		
		TermWrapper tw = new TermWrapper();
		tw.setSerial(comment.getSerial());
		tw.setRank(comment.getRank());
		tw.setTerm(service.getTerm());
		
		return tw;
		
	}
	
}
