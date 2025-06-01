package com.kneissler.util.injection.controller;

public class ControllerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ControllerException(String string) {
		super(string);
	}

	public ControllerException(String string, Exception e) {
		super(string, e);
	}

}
