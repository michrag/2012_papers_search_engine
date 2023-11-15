package utilities;

public class StringSanitizer
{

    public static String sanitize(String str)
    {
        str = str.replaceAll("\\s{2,}", " "); // spazi multipli (>=2) -> uno spazio
        str = str.replace("- ", ""); // trattinoSpazio -> nulla

        return str;
    }

}
