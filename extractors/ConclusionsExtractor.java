package extractors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utilities.StringSanitizer;


public class ConclusionsExtractor
{
    private File inputFile;

    private Pattern conclusionPattern;

    private Pattern endConclusionPattern;

    public ConclusionsExtractor(File inputFile)
    {
        this.inputFile = inputFile;

        conclusionPattern = Pattern.compile("^(\\d|\\w+)?((\\.\\s)?(\\.)?(\\s)?){1}conclusions?(\\s*\\w\\s*)*\\s*$", Pattern.CASE_INSENSITIVE);

        endConclusionPattern = Pattern.compile("^(\\d|\\w+)?((\\.\\s)?(\\.)?(\\s)?)?\\w+\\s*$", Pattern.CASE_INSENSITIVE);
    }

    public boolean startConclusion(String str)
    {
        Matcher conclusionMatcher = conclusionPattern.matcher(str);
        return conclusionMatcher.find();
    }


    private boolean endConclusion(String str)
    {
        Matcher endConclusionMatcher = endConclusionPattern.matcher(str);
        return endConclusionMatcher.find();
    }


    public String getConclusion() throws FileNotFoundException
    {
        Scanner inputFileScanner = new Scanner(inputFile);

        String conclusion = new String();

        boolean conclusionFound = false;

        String str = inputFileScanner.nextLine();

        while(!conclusionFound && inputFileScanner.hasNextLine())
        {
            if(startConclusion(str))
            {
                conclusionFound = true;
            }

            str = inputFileScanner.nextLine();
        }

        if(conclusionFound)
        {
            while(!endConclusion(str) && inputFileScanner.hasNextLine())
            {
                conclusion = conclusion.concat(str);
                conclusion = conclusion.concat(" ");

                str = inputFileScanner.nextLine();
            }
        }

        inputFileScanner.close();

        //System.out.print(" conclusion extracted! ");

        conclusion = StringSanitizer.sanitize(conclusion);

        return conclusion;
    }

}
