import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class TestManager {

    private static final String[] VALID_ARGS = new String[]{"arg1", "arg2", "arg3", "arg4", "arg5", "arg6", "arg7", "arg8", "arg9"};

    @Test
    public void testPostExec_GivenValidArgsWithoutGenerateKeystore_WhenCalled_ThenReturnSameArgs() throws FileNotFoundException {
        // GIVEN: des arguments valides sans l'argument "buildkeystore"
        String[] args = VALID_ARGS.clone();

        // WHEN: on appelle la méthode postExec avec les arguments
        String[] result = Manager.postExec(args);

        // THEN: la méthode doit retourner les mêmes arguments
        assertArrayEquals(args, result);
    }

    @Test
    public void testPostExec_GivenValidArgsWithGenerateKeystore_WhenCalled_ThenReturnSameArgs() throws FileNotFoundException {
        // GIVEN: des arguments valides avec l'argument "buildkeystore"
        String[] args = new String[]{Manager.ARG_GENERATE_KEYSTORE};

        // WHEN: on appelle la méthode postExec avec les arguments
        String[] result = Manager.postExec(args);

        // THEN: la méthode doit retourner les mêmes arguments
        assertArrayEquals(args, result);
    }

    @Test
    public void testPostExec_GivenValidArgsWithExistingStatusAndConfigurationFiles_WhenCalled_ThenReturnSubsetOfArgs() throws FileNotFoundException {
        // GIVEN: des arguments valides avec des fichiers de statut et de configuration existants
        String[] args = new String[]{"status_file_path", "config_file_path", "arg1", "arg2", "arg3", "arg4", "arg5", "arg6", "arg7", "arg8", "arg9"};

        // WHEN: on appelle la méthode postExec avec les arguments
        String[] result = Manager.postExec(args);

        // THEN: la méthode doit retourner un sous-ensemble des arguments, sans le chemin des fichiers de statut et de configuration
        assertEquals(args.length - 2, result.length);
        assertTrue(StringUtils.isNotBlank(result[0]));
        assertTrue(StringUtils.isNotBlank(result[1]));
    }

    @Test
    public void testPostExec_GivenValidArgsWithMissingStatusOrConfigurationFiles_WhenCalled_ThenExitWithErrorCode() throws FileNotFoundException {
        // GIVEN: des arguments valides avec un fichier de statut ou de configuration manquant
        String[] args = new String[]{"missing_status_file_path", "missing_config_file_path", "arg1", "arg2", "arg3", "arg4", "arg5", "arg6", "arg7", "arg8", "arg9"};

        // WHEN: on appelle la méthode postExec avec les arguments
        try {
            Manager.postExec(args);
        } catch (Exception e) {
            // THEN: la méthode doit quitter le programme avec un code d'erreur
            assertEquals(-1, e.getMessage());
        }
    }
}
