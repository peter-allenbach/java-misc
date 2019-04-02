import java.io.*;
import java.util.*;
import java.nio.file.*;
import org.ietf.jgss.*;

import java.awt.*;
import javax.swing.*;

class ExtendedOid extends Oid
{
    static Map<String,String> map=new TreeMap<String,String>();

    static
    {
        Scanner scanner=null;
        try
        { 
            scanner=new Scanner(new File("OID-Mapping.txt")); 
        } catch (Exception e) {}
        while(scanner.hasNext())
        { 
            String key=scanner.next();
            String value=scanner.next();
            map.put(key,value);
        }
    }

    public ExtendedOid(byte b[]) throws Exception
    {
        super(b);
    }

    public ExtendedOid(String s) throws Exception
    {
        super(s);
    }

    static public void printMap()
    {
        map.forEach((k, v) -> System.out.println(k + " " + v));
    }

    public String toString()
    {
        if(map.containsKey(super.toString()))
        {
            return super.toString() + " (" + map.get(super.toString()).replace("-"," ") + ")";
        }
        else
        {
            return super.toString();
        }
    }

    public static void main(String argv[]) throws Exception
    {
        printMap();
    }
}
