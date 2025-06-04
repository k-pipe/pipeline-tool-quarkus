package pipelining.util.injection.controller;

import java.util.List;

public interface Controller {	

	String getPathPrefix();
	
	List<EndPoint> getEndPoints();

}
