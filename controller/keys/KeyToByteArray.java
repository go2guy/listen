import java.io.*;
import java.util.Arrays;

public class KeyToByteArray
{
    public static void main(String[] args)
    {
        if(args.length != 1)
        {
            System.err.println("Usage: java KeyToByteArray <input-key-file>");
            System.exit(1);
        }

        File file = new File(args[0]);
        if(!file.exists() || !file.isFile() || !file.canRead())
        {
            System.err.println("File [" + args[0] + "] is not a file or cannot be accessed");
            System.exit(1);
        }

        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null)
            {
                if(line.startsWith("-----"))
                {
                    continue;
                }

                builder.append(line);
            }

            String encoded = builder.toString();
            byte[] bytes = encoded.getBytes();

            System.out.println(Arrays.toString(bytes));
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}