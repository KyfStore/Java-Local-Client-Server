import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.SwingUtilities;

public class Server {
    public static void main(String[] args)
    {
        Path serverJSON = Paths.get("public/dependencies/server.json");
        if (Files.exists(serverJSON))
        {
            System.out.println("Successfully launched server!");
            SwingUtilities.invokeLater(ServerGUI::new);
        } else {
            System.err.println("Could not find server attributes. Return Code=1");;
            return;
        }
    }
}