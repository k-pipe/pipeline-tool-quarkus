package com.kneissler.util.injection.controller;

import com.kneissler.util.injection.jar.DynamicJarSet;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.jkube.application.Application.fail;

public class ControllerSet {

	private List<EndPoint> endPoints;

	public ControllerSet(DynamicJarSet jarSet) {
		this.endPoints = new ArrayList<>();
		scanEnpoints(jarSet);
	}

	public List<EndPoint> getEndPoints() {
		return endPoints;
	}

	private void scanEnpoints(DynamicJarSet jarSet) {
		jarSet.forEachLoadedClass(this::scanEndpointsInClass);
	}

	private void scanEndpointsInClass(Class<?> aClass) {
		if (Controller.class.isAssignableFrom(aClass)) {
			try {
				List<EndPoint> add = ((Controller)(aClass.getDeclaredConstructor().newInstance())).getEndPoints();
				System.out.println(aClass.getCanonicalName() + " --> "+add.size()+" endpoints found");
				endPoints.addAll(add);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				fail("Could not invoke getEndPoints() for controller "+aClass.getSimpleName()+": "+e.getMessage());
			}
		} else {
			System.out.println(aClass.getCanonicalName() + " not assignable");			
		}
	}

	public void addEndPoint(EndPoint endpoint) {
		endPoints.add(endpoint);
	}

}
