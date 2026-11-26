
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LookupBehavior {

    private static final Logger logger =
            LogManager.getLogger(LookupBehavior.class);

    public static void main(String[] args) {
        System.out.println("Inicio de la prueba controlada");

        // Expresión inocua: consulta información de Java.
        // No utiliza JNDI ni recursos externos.
        logger.error("PRUEBA_LOOKUP: ${java:version}");

        System.out.println("Fin de la prueba controlada");
    }
}
