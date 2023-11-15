package utilities;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class PDFtoTXTconverter
{

    private File inputPDFfile;


    public PDFtoTXTconverter(File inputPDFfile)
    {
        this.inputPDFfile = inputPDFfile;
    }


    public String getFullText(int startPage, int endPage) throws IOException
    {
        StringWriter strwrt = new StringWriter();

        PDDocument pdDoc = PDDocument.load(inputPDFfile);
        PDFTextStripper stripper = new PDFTextStripper();

        System.out.print("extracting text ");

        if((startPage > 0) && (endPage > 0) && (endPage >= startPage))
        {
            System.out.print("(page " + startPage + " to " + endPage + ") ");
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);
        }

        System.out.print("... ");

        stripper.writeText(pdDoc, strwrt);

        if(pdDoc != null)
        {
            pdDoc.close();
        }

        strwrt.close();

        System.out.println("done!");

        return strwrt.toString();
    }

}
