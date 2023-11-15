package extractors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TitleExtractor
{

    private File inputFile;


    public TitleExtractor(File inputFile)
    {
        this.inputFile = inputFile;
    }


    public String getFirstLine() throws FileNotFoundException
    {
        Scanner inputFileScanner = new Scanner(inputFile);

        String title = new String();

        title = title.concat(inputFileScanner.nextLine()); // IPOTIZZO che il titolo sia esattamente la prima linea...!

        while(title.length() < 10)
        {
            title = inputFileScanner.nextLine();
        }

        inputFileScanner.close();

        //      System.out.println(title);

        //System.out.println();

        return title;
    }

}
