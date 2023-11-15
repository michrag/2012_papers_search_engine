package extractors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utilities.StringSanitizer;

public class KeywordsExtractor
{
    private File inputFile;

    private IntroductionExtractor introEx;

    private Pattern keywordsPattern;
    private Pattern indexTermsPattern;


    public KeywordsExtractor(File inputFile, IntroductionExtractor introEx)
    {
        this.inputFile = inputFile;
        this.introEx = introEx;

        keywordsPattern = Pattern.compile("^\\s*Keywords\\W+"); // case SENSITIVE (altrimenti se c'è "keywords" prima s'incasina!!!)
        indexTermsPattern = Pattern.compile("^\\s*Index Terms\\W+");
    }


    public boolean startKeyordsOrIndexTerms(String str)
    {
        return (startKeywords(str) || startIndexterms(str));
    }


    private boolean startKeywords(String str)
    {
        Matcher keywordsMatcher = keywordsPattern.matcher(str);
        return keywordsMatcher.find();
    }


    private boolean startIndexterms(String str)
    {
        Matcher indexTermsMatcher = indexTermsPattern.matcher(str);
        return indexTermsMatcher.find();
    }


    private String getFirstWord(Pattern notLetter, String str, String keywords)
    {
        Matcher notLetterMacther = notLetter.matcher(str);

        if(notLetterMacther.find())
        {
            String firstWord = str.substring(notLetterMacther.end());

            if(firstWord.length() > 0)
            {
                keywords = keywords.concat(firstWord);
                keywords = keywords.concat(" ");
            }
        }

        return keywords;
    }


    public String getKeywords() throws FileNotFoundException
    {
        Scanner inputFileScanner = new Scanner(inputFile);

        String keywords = new String();

        boolean keywordsFound = false;

        String str = inputFileScanner.nextLine();

        while(!keywordsFound && inputFileScanner.hasNextLine())
        {
            if(startKeywords(str))
            {
                keywordsFound = true;
                Pattern notLetter = Pattern.compile("\\W+"); // il/i primo/i carattere non lettera.
                keywords = getFirstWord(notLetter, str, keywords);
            }

            if(startIndexterms(str))
            {
                keywordsFound = true;
                Pattern notLetter = Pattern.compile("s\\W+"); // il/i primo/i carattere non lettera dopo la "s" di termS ...!
                keywords = getFirstWord(notLetter, str, keywords);
            }

            str = inputFileScanner.nextLine();
        }


        if(keywordsFound)
        {
            while(!introEx.startIntroduction(str) && inputFileScanner.hasNextLine())
            {
                keywords = keywords.concat(str);
                keywords = keywords.concat(" ");

                str = inputFileScanner.nextLine();
            }
        }

        inputFileScanner.close();

        //System.out.print(" keywords extracted! ");

        keywords = StringSanitizer.sanitize(keywords);

        return keywords;
    }

}
