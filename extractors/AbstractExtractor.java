package extractors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utilities.StringSanitizer;


public class AbstractExtractor
{

    private File inputFile;

    private IntroductionExtractor introEx;
    private KeywordsExtractor keywEx;

    private Pattern abstractPattern;

    public AbstractExtractor(File inputFile, IntroductionExtractor introEx, KeywordsExtractor keywEx)
    {
        this.inputFile = inputFile;

        this.introEx = introEx;
        this.keywEx = keywEx;

        abstractPattern = Pattern.compile("^\\s*abstract\\W*", Pattern.CASE_INSENSITIVE);
    }


    public boolean startAbstract(String str)
    {
        Matcher abstractMatcher = abstractPattern.matcher(str);
        return abstractMatcher.find();
    }


    public String getAbstract() throws FileNotFoundException
    {
        Scanner inputFileScanner = new Scanner(inputFile);

        String abstr = new String();

        boolean abstractFound = false;

        String str = inputFileScanner.nextLine();

        while(!abstractFound && inputFileScanner.hasNextLine())
        {
            if(startAbstract(str))
            {
                abstractFound = true;

                Pattern notLetter = Pattern.compile("\\W+"); // il/i primo/i carattere non lettera.
                Matcher notLetterMacther = notLetter.matcher(str);

                if(notLetterMacther.find())
                {
                    String firstWord = str.substring(notLetterMacther.end());

                    if(firstWord.length() > 0)
                    {
                        abstr = abstr.concat(firstWord);
                        abstr = abstr.concat(" ");
                    }
                }
            }

            str = inputFileScanner.nextLine();
        }

        if(abstractFound)
        {
            while((!(introEx.startIntroduction(str) || keywEx.startKeyordsOrIndexTerms(str))) && inputFileScanner.hasNextLine())
            {
                abstr = abstr.concat(str);
                abstr = abstr.concat(" ");

                str = inputFileScanner.nextLine();
            }
        }

        inputFileScanner.close();

        //System.out.print(" abstract extracted! ");

        abstr = StringSanitizer.sanitize(abstr);

        return abstr.replace("Abstract-", "");
    }

}
