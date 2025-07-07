/* 
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife;

/**
 * Main entry point for the TheKnife application.
 * <p>
 * This class serves as the primary entry point for the application and
 * delegates to the Launcher class to start the JavaFX application.
 * </p>
 * <p>
 * The separation between Main and Launcher allows for better compatibility
 * with different execution environments and packaging methods.
 * </p>
 */
public class Main {
    /**
     * Default constructor for the Main class.
     * <p>
     * This constructor is not meant to be used directly as this class only provides
     * static methods. The class is not designed to be instantiated.
     * </p>
     */
    public Main() {
        // Default constructor - not meant to be used
    }
    /**
     * The main method that starts the application.
     * <p>
     * This method delegates to the Launcher class to initialize and
     * start the JavaFX application.
     * </p>
     *
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        Launcher.main(args);
    }
}
