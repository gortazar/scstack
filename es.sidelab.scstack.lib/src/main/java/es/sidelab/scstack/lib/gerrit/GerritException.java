package es.sidelab.scstack.lib.gerrit;

public class GerritException extends Exception {

	public GerritException(String message) {
		super(message);
	}
	
	public GerritException(String message, Throwable t) {
		super(message, t);
	}

}
