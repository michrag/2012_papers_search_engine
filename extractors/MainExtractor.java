package extractors;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import utilities.PDFtoTXTconverter;


public class MainExtractor
{

    private static boolean extractPDF = true;

    private static boolean oneFolderForEachPDF = true;
    private static String pdfFolder;
    private static String txtFolder = "txts/";
    //  private static boolean oneFolderForEachPDF = true;
    //  private static String pdfFolder = "pdfSubdir/";
    //  private static String txtFolder = "txtSubdir/";


    public static ArrayList<File> getAllTheFilesIn(File[] files)
    {
        ArrayList<File> allTheFiles = new ArrayList<File>();

        for(File file : files)
        {
            if(file.isDirectory())
            {
                allTheFiles.addAll(getAllTheFilesIn(file.listFiles()));
            }
            else
            {
                allTheFiles.add(file);
            }
        }

        return allTheFiles;
    }


    private static File getOutputFile(File inputfile, String suffix)
    {
        String txtFilePath = new String(inputfile.getPath());

        txtFilePath = txtFilePath.replace(pdfFolder, txtFolder);
        txtFilePath = txtFilePath.replace(inputfile.getName(), ""); // voglio il path senza nome del file!

        File f = new File(txtFilePath);
        f.mkdirs(); // con la S, fondamentale!!!

        txtFilePath = txtFilePath.concat(inputfile.getName()); // riattacco il nome del file
        txtFilePath = txtFilePath.substring(0, txtFilePath.length() - 4); // elimino estensione (col .)

        txtFilePath += suffix;
        txtFilePath += ".txt";

        File outputFile = new File(txtFilePath);

        return outputFile;
    }

    // seconda versione in cui creo una cartella per ciascun pdf
    private static File getOutputFileCreatingSubfolderForEachPDF(File inputfile, String suffix)
    {
        String txtFilePath = new String(inputfile.getPath());

        txtFilePath = txtFilePath.replace(pdfFolder, txtFolder);
        txtFilePath = txtFilePath.substring(0, txtFilePath.length() - 4); // elimino estensione (col .)

        if(inputfile.getName().endsWith(".pdf"))
        {
            txtFilePath += "/"; // così creo la cartella col nome del pdf di partenza - devi farlo solo la priam volta fava!
            //txtFilePath = txtFilePath.replace(inputfile.getName(), ""); // voglio il path senza nome del file!

            File f = new File(txtFilePath);
            f.mkdirs(); // con la S, fondamentale!!!

            //txtFilePath = txtFilePath.concat(inputfile.getName()); // riattacco il nome del file
            //txtFilePath = txtFilePath.substring(0, txtFilePath.length()-4); // elimino estensione (col .)
        }

        txtFilePath += suffix;
        txtFilePath += ".txt";

        File outputFile = new File(txtFilePath);

        return outputFile;
    }


    private static void writeStringToFile(String toWrite, File file) throws IOException
    {
        if(toWrite.length() > 0)
        {
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            bufWriter.write(toWrite);
            bufWriter.close();
        }
    }


    // --------------------------------------- MAIN ------------------------------------------------
    public static void run(String pdfsSource)
    {
        pdfFolder = pdfsSource;

        //clears /txts directory
        if(new File("txts/").listFiles() == null)
        {
            new File("txts/").mkdir();
        }
        else if(new File("txts/").listFiles().length > 0)
        {
            removeDirectory(new File("txts/"));
            new File("txts/").mkdir();
        }

        // estrazione pdf -> txt
        File[] pdfFiles = new File(pdfFolder).listFiles();
        ArrayList<File> allPDFs = getAllTheFilesIn(pdfFiles);

        for(File f : allPDFs)
        {
            if(f.getName().endsWith(".pdf"))   // è un pdf
            {
                if(extractPDF)
                {
                    System.out.print(f.getName() + " - ");

                    try
                    {
                        PDFtoTXTconverter pdf2txtConverter = new PDFtoTXTconverter(f);
                        String fullText = pdf2txtConverter.getFullText(0, 0); // estraggo TUTTO il pdf

                        if(oneFolderForEachPDF)
                        {
                            writeStringToFile(fullText, getOutputFileCreatingSubfolderForEachPDF(f, ""));
                        }
                        else
                        {
                            writeStringToFile(fullText, getOutputFile(f, ""));
                        }
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }


        // estrazione sezioni da txt
        File[] txtFiles = new File(txtFolder).listFiles();
        ArrayList<File> allTXTs = getAllTheFilesIn(txtFiles);

        for(File f : allTXTs)
        {
            if(f.getName().endsWith(".txt") && !f.getName().contains("-"))   // è il txt estratto dal pdf
            {
                try
                {
                    TitleExtractor titleEx = new TitleExtractor(f);
                    IntroductionExtractor introEx = new IntroductionExtractor(f);
                    KeywordsExtractor keywEx = new KeywordsExtractor(f, introEx);
                    AbstractExtractor abstEx = new AbstractExtractor(f, introEx, keywEx);
                    ConclusionsExtractor concEx = new ConclusionsExtractor(f);

                    //                  System.out.print(f.getName() + " - ");

                    if(oneFolderForEachPDF)
                    {
                        //writeStringToFile(titleEx.getFirstLine(), getOutputFileCreatingSubfolderForEachPDF(f, "title"));
                        //title + abstract + keywords-->one section
                        writeStringToFile(
                            titleEx.getFirstLine() + " " +
                            abstEx.getAbstract() + " " +
                            keywEx.getKeywords(), getOutputFileCreatingSubfolderForEachPDF(f, "abstract"));
                        //writeStringToFile(keywEx.getKeywords(), getOutputFileCreatingSubfolderForEachPDF(f, "keywords"));
                        writeStringToFile(introEx.getIntroduction(), getOutputFileCreatingSubfolderForEachPDF(f, "intro"));
                        writeStringToFile(concEx.getConclusion(), getOutputFileCreatingSubfolderForEachPDF(f, "conclusion"));
                    }
                    else
                    {
                        writeStringToFile(titleEx.getFirstLine(), getOutputFile(f, "-Title"));
                        writeStringToFile(abstEx.getAbstract(), getOutputFile(f, "-Abstract"));
                        writeStringToFile(keywEx.getKeywords(), getOutputFile(f, "-Keywords"));
                        writeStringToFile(introEx.getIntroduction(), getOutputFile(f, "-Intro"));
                        writeStringToFile(concEx.getConclusion(), getOutputFile(f, "-Conclusion"));
                    }

                    //                  System.out.println();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }

            }
        }

    }

    static public boolean removeDirectory(File path)
    {
        if(path.exists())
        {
            File[] files = path.listFiles();

            for(int i = 0; i < files.length; i++)
            {
                if(files[i].isDirectory())
                {
                    removeDirectory(files[i]);
                }
                else
                {
                    files[i].delete();
                }
            }
        }

        return(path.delete());
    }


}
