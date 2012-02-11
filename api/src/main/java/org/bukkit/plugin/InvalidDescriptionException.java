package org.bukkit.plugin;

/**
 * Thrown when attempting to load an invalid PluginDescriptionFile
 */
public class InvalidDescriptionException extends Exception {
    private static final long serialVersionUID = 5721389122281775894L;

    /**
     * Constructs a new InvalidDescriptionException based on the given Exception
     *
     * @param message Brief message explaining the cause of the exception
     * @param cause Exception that triggered this Exception
     */
    public InvalidDescriptionException(final Throwable cause, final String message) {
        super(message + (cause != null ? ": " + cause.getMessage() : ""), cause);
    }

    /**
     * Constructs a new InvalidDescriptionException based on the given Exception
     *
     * @param throwable Exception that triggered this Exception
     */
    public InvalidDescriptionException(final Throwable cause) {
        this(cause, "Invalid plugin.yml");
    }

    /**
     * Constructs a new InvalidDescriptionException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public InvalidDescriptionException(final String message) {
        this(null, message);
    }

    /**
     * Constructs a new InvalidDescriptionException
     */
    public InvalidDescriptionException() {
        this(null, "Invalid plugin.yml");
    }
}
