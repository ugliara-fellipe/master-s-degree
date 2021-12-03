/**
 * 
 */
package error;

import java.io.PrintWriter;

/** Represents a single error in the project
 * @author José
 *
 */
public class ProjectError  {

	public ProjectError(String message) {
		this.message = message;
	}

	public void print( PrintWriter printWriter ) {
		printWriter.println(message);
	}
	
	/**
	 * the error message
	 */
	private String message;
}
