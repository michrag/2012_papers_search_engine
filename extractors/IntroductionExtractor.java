package extractors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utilities.StringSanitizer;


public class IntroductionExtractor
{
    private File inputFile;

    private Pattern introPattern;

    private Pattern endIntroPattern;

    public IntroductionExtractor(File inputFile)
    {
        this.inputFile = inputFile;

        introPattern = Pattern.compile("^(1|I|A)((\\.\\s)?(\\.)?(\\s)?){1}introduction(\\s*\\w\\s*)*\\s*$", Pattern.CASE_INSENSITIVE);

        endIntroPattern = Pattern.compile("^(2|II)((\\.\\s)?(\\.)?(\\s)?){1}((\\w|\\-|\\.)\\s*)*\\s*$");
    }

    public boolean startIntroduction(String str)
    {
        Matcher introMatcher = introPattern.matcher(str);
        return introMatcher.find();
    }


    private boolean endIntroduction(String str)
    {
        Matcher endIntroMatcher = endIntroPattern.matcher(str);
        return endIntroMatcher.find();
    }


    public String getIntroduction() throws FileNotFoundException
    {
        Scanner inputFileScanner = new Scanner(inputFile);

        String intro = new String();

        boolean introFound = false;

        String str = inputFileScanner.nextLine();

        while(!introFound && inputFileScanner.hasNextLine())
        {
            if(startIntroduction(str))
            {
                introFound = true;
            }

            str = inputFileScanner.nextLine();
        }

        if(introFound)
        {
            while(!endIntroduction(str) && inputFileScanner.hasNextLine())
            {
                intro = intro.concat(str);
                intro = intro.concat(" ");

                str = inputFileScanner.nextLine();
            }
        }

        inputFileScanner.close();

        //System.out.print(" introduction extracted! ");

        intro = StringSanitizer.sanitize(intro);

        return intro;
    }

}
