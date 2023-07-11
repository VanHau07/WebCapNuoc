package vn.webcapnuoc.hddt.user.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import com.api.message.MsgPage;

import vn.webcapnuoc.hddt.utility.Commons;

public class AbstractDAO {
	public Commons commons = new Commons();
	
	public List<Document> createFacetForSearchNotSort(MsgPage page){
		List<Document> r = new ArrayList<Document>();
		
		int pageNo = commons.calcPageNo(page.getPageNo());
		int size = commons.calcPageSize(page.getSize());
		
//		if(null == page.getFieldSort() || "".equals(page.getFieldSort()))
//			r.add(new Document("$sort", new Document("_id", -1)));
//		else
//			r.add(new Document("$sort", new Document(page.getFieldSort(), page.getTypeSort())));

		r.add(
			new Document("$facet", 
				new Document("meta", 
					Arrays.asList(
						new Document("$count", "total")
					)
				)
				.append("data", 
					Arrays.asList(
						new Document("$skip", size * (pageNo - 1))
						, new Document("$limit", size)
					)
				)
			)
		);
		r.add(new Document("$unwind", "$meta"));
		r.add(new Document("$project", new Document("total", "$meta.total").append("data", 1)));
		
		return r;
	}
	
	public List<Document> createFacetForSearch(MsgPage page){
		List<Document> r = new ArrayList<Document>();
		
		int pageNo = commons.calcPageNo(page.getPageNo());
		int size = commons.calcPageSize(page.getSize());
		
		if(null == page.getFieldSort() || "".equals(page.getFieldSort()))
			r.add(new Document("$sort", new Document("_id", -1)));
		else
			r.add(new Document("$sort", new Document(page.getFieldSort(), page.getTypeSort())));

		r.add(
			new Document("$facet", 
				new Document("meta", 
					Arrays.asList(
						new Document("$count", "total")
					)
				)
				.append("data", 
					Arrays.asList(
						new Document("$skip", size * (pageNo - 1))
						, new Document("$limit", size)
					)
				)
			)
		);
		r.add(new Document("$unwind", "$meta"));
		r.add(new Document("$project", new Document("total", "$meta.total").append("data", 1)));
		
		return r;
	}
	
	public List<Document> createFacetForSearch(MsgPage page, String defaultField){
		List<Document> r = new ArrayList<Document>();
		
		int pageNo = commons.calcPageNo(page.getPageNo());
		int size = commons.calcPageSize(page.getSize());
		
		if(null == page.getFieldSort() || "".equals(page.getFieldSort()))
			r.add(new Document("$sort", new Document(defaultField, -1)));
		else
			r.add(new Document("$sort", new Document(page.getFieldSort(), page.getTypeSort())));

		r.add(
			new Document("$facet", 
				new Document("meta", 
					Arrays.asList(
						new Document("$count", "total")
					)
				)
				.append("data", 
					Arrays.asList(
						new Document("$skip", size * (pageNo - 1))
						, new Document("$limit", size)
					)
				)
			)
		);
		r.add(new Document("$unwind", "$meta"));
		r.add(new Document("$project", new Document("total", "$meta.total").append("data", 1)));
		
		return r;
	}
}
