import java.io.*;
import java.util.*;
import java.nio.file.*;
import org.ietf.jgss.*;

import java.awt.*;
import javax.swing.*;

class A
{
    byte b[] = null;
    ExtendedOid oid=null;

    public A(String argv[]) throws Exception
    {
        Path path=null;

        if(argv.length==0)
        {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setPreferredSize(new Dimension(800,600));
            setFileChooserFont(jFileChooser.getComponents());
            int result=jFileChooser.showOpenDialog(null);

            if(result==JFileChooser.APPROVE_OPTION)
            {
                path=jFileChooser.getSelectedFile().toPath(); 
            }
            else
            {
                System.exit(0);
            }
        }
        else if (argv.length==1)
        {
            path = FileSystems.getDefault().getPath(argv[0]);
        }
        else
        {
            System.out.println("Usage: java A [file name]");
            System.exit(0);
       }

        String s=new String(Files.readAllBytes(path));
        s=s.replaceAll("-----(BEGIN|END) [A-Z]+-----","");
        b = Base64.getMimeDecoder().decode(s);
        P.print(0,b);
    }

    public void setFileChooserFont(Component comp[])
    {
        Font font = new Font("dialog",Font.PLAIN,18);

        for(int i = 0; i < comp.length; i++)
        {
            if(comp[i] instanceof Container)
            {
                setFileChooserFont(((Container)comp[i]).getComponents());
            }

            try
            {
                comp[i].setFont(font);
            }
            catch(Exception e)
            {
            }
        }
    }

    public static void main(String argv[]) throws Exception
    {
        new A(argv);
    }
}
