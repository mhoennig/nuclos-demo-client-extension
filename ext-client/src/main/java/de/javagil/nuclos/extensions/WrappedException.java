package de.javagil.nuclos.extensions;


/**
 * Wraps checked exceptions as an unchecked exception.
 *  
 * @author (2015) <a href="mailto:michael@hoennig.de">Michael Hoennig</a> 
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 */
@SuppressWarnings("serial")
public class WrappedException extends RuntimeException {

	public WrappedException(Exception ex) {
		super(ex);
	}

	public WrappedException(String message, Exception exc) {
		super(message, exc);
	}

}
