package application.commands;

public class CommandErrorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7762164395805817369L;

	public CommandErrorException() {
		super();
	}
	
	public CommandErrorException(String badCommand) {
		super(badCommand);
		System.out.println("B³¹d w komendzie: " + badCommand);
	}
	
	public CommandErrorException(String badCommand, Throwable cause) {
		super(badCommand, cause);
		System.out.println("B³¹d w komendzie: " + badCommand);
	}
	
	public CommandErrorException(Throwable cause) {
		super(cause);
	}
}
