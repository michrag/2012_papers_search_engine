package utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import models.Document;
import models.Document.SectionParams;
import extractors.MainExtractor;

public class DocumentBoxInitializer
{

    private static String txtsSourcePath = "txts/";

    /*
     * generates a handy structure that contains a folder for each document;
     * each document folder contains a subfolder for every section;
     * every subfolder contains 3 files, first with extracted content, second with extracted topics,
     * third with topics metadata (useful when loading topics from an existing box);
     * each document in the box folder corresponds to a number from 0 and 'howManyDocsInTheBox'
     */
    public static void initialize(String pdfsSource, String documentBoxPath, int howManyDocsInTheBox)
    {

        try
        {
            System.out.println("starting .pdf to .txt extraction...");
            MainExtractor.run(pdfsSource);
            BufferedWriter writer;
            BufferedReader reader;
            String base = documentBoxPath;

            for(int i = 0; i < howManyDocsInTheBox; i++)
            {

                File currentTxtFile = new File(txtsSourcePath).listFiles()[i];
                String documentPath = currentTxtFile.getPath();
                String documentName = currentTxtFile.getName();
                new File(base + i).mkdirs();
                writer = new BufferedWriter(new FileWriter(base + i + "/title.txt"));
                writer.write(documentName);
                writer.close();

                for(SectionParams s : Document.SectionParams.values())
                {

                    String section = s.getLabel();
                    String currentSection = documentPath + "/" + section + ".txt";

                    if(new File(currentSection).exists())
                    {

                        reader = new BufferedReader(new FileReader(new File(currentSection)));
                        String content = reader.readLine();

                        if(content.length() > 10)
                        {

                            String fileName;
                            String dirName = i + "/" + section;
                            new File(base + dirName).mkdirs();
                            fileName = base + dirName + "/" + section + ".txt";
                            File file = new File(fileName);
                            file.setWritable(true);
                            writer = new BufferedWriter(new FileWriter(fileName));
                            writer.write(content);
                            writer.close();

                            fileName = base + dirName + "/extracted topics.txt";
                            file = new File(fileName);
                            file.setWritable(true);
                            writer = new BufferedWriter(new FileWriter(
                                                            new File(fileName)));
                            writer.write("ready");
                            writer.close();

                            fileName = base + dirName + "/topics ID.txt";
                            file = new File(fileName);
                            file.setWritable(true);
                            writer = new BufferedWriter(new FileWriter(
                                                            new File(fileName)));
                            writer.write("ready");
                            writer.close();
                        }
                    }//txt section exists
                }//sections
            }//documents
        }
        catch(Exception e)
        {
            e.getMessage();
            e.printStackTrace();
            System.out.println("something wrong creating the box! exiting...");
            System.exit(0);
        }
    }

}
