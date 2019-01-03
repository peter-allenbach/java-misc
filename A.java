import java.util.*;
import java.nio.file.*;
import org.ietf.jgss.*;

class A
{
    byte b[] = null;
    Oid oid=null;

    public A() throws Exception
    {
        Path inPath = FileSystems.getDefault().getPath("b.csr");
        b = Base64.getMimeDecoder().decode(Files.readAllBytes(inPath));
        parse(0,b);
    }

    void parse(int level,byte asn1Object[]) throws Exception
    {
        int tag=Byte.toUnsignedInt(asn1Object[0]);
        int length=getLength(asn1Object);
        int lengthOfLength=getLengthOfLength(asn1Object);

        byte v[]=Arrays.copyOfRange(asn1Object,lengthOfLength+1, length+lengthOfLength+1);

        if (tag == 0x30)
        {
            emitSpacing(level);
            emit("sequence");
            emitnl();
            byte first[], rest[];
            rest=Arrays.copyOf(v,v.length);
            while(rest.length>0)
            {
                length=getLength(rest);
                lengthOfLength=getLengthOfLength(rest);
                first=Arrays.copyOfRange(rest,0,length+lengthOfLength+1);
                rest=Arrays.copyOfRange(rest,length+lengthOfLength+1,rest.length);
                parse(level+1,first);
            }
        }
        else if (tag == 0x02)
        {
            emitSpacing(level);
            emit("integer: ");
            emitByteArray(v);
            emitnl();
        }
        else if (tag == 0x03)
        {
            emitSpacing(level);
            emit("bit string: ");
            emitElidedByteArray(v);
            emitnl();
        }
        else if (tag == 0x05)
        {
            emitSpacing(level);
            emit("null");
            emitnl();
        }
        else if (tag == 0x06)
        {
            emitSpacing(level);
            emit("oid: ");
            oid = new Oid(asn1Object);
            emit(oid.toString());
            emitnl();  
        }
        else if (tag == 0x0c)
        {
            emitSpacing(level);
            emit("utf8string: ");
            emit(new String(v));
            emitnl();  
        }
        else if (tag == 0x13)
        {
            emitSpacing(level);
            emit("printable string: ");
            emit(new String(v));
            emitnl();
        }
        else if (tag == 0x17)
        {
            emitSpacing(level);
            emit("utc time: ");
            emitByteArray(v);
            emitnl();
        }
        else if (tag == 0x18)
        {
            emitSpacing(level);
            emit("generalized time: ");
            emitByteArray(v);
            emitnl();
        }
        else if (tag == 0x31)
        {
            emitSpacing(level);
            emit("set ");
            emitnl();
            byte first[], rest[];
            rest=Arrays.copyOf(v,v.length);
            while(rest.length>0)
            {
                length=getLength(rest);
                lengthOfLength=getLengthOfLength(rest);
                first=Arrays.copyOfRange(rest,0,length+lengthOfLength+1);
                rest=Arrays.copyOfRange(rest,length+lengthOfLength+1,rest.length);
                parse(level+1,first);
            }
        }
        else if ((tag&0xc0)==0x80)
        {
            emitSpacing(level);
            emit("Context-specific Tag: ");
            emitElidedByteArray(v);
            emitnl();
        }
        else if ((tag&0x1f)==0x1f)
        {
            System.out.println("Sorry, no support for multibyte tags");
            System.exit(0);
        }
        else 
        {
            System.out.printf("Unknown tag: %02x\n",(tag));
            throw(new Exception());
        }
    }

    int getLength(byte asn1Object[]) throws Exception
    {
        if(Byte.toUnsignedInt(asn1Object[1])<128)
        {
            return asn1Object[1];
        }
        else if(Byte.toUnsignedInt(asn1Object[1])==0x81) 
        {
            return Byte.toUnsignedInt(asn1Object[2]);
        }
        else if(Byte.toUnsignedInt(asn1Object[1])==0x82) 
        {
            return (256*Byte.toUnsignedInt(asn1Object[2]))+
                    Byte.toUnsignedInt(asn1Object[3]);
        }
        else throw(new Exception());
    }

    int getLengthOfLength(byte asn1Object[]) throws Exception
    {
        if(Byte.toUnsignedInt(asn1Object[1])<128)
        {
            return 1;
        }
        else if(Byte.toUnsignedInt(asn1Object[1])==0x81) 
        {
            return 2;
        }
        else if(Byte.toUnsignedInt(asn1Object[1])==0x82) 
        {
            return 3;
        }
        else throw(new Exception());
    }

    void emit(String s)
    {
    	System.out.print(s);
    }

    void emitnl()
    {
    	System.out.println();
    }

    void emitSpacing(int level)
    {
        for (int i=0;i<level;++i) System.out.print("  ");
    }

    void emitByteArray(byte b[])
    {
        for (int i = 0; i < b.length; ++i)
    	{
            System.out.printf("%02x ", b[i]);
    	}
    }

    void emitElidedByteArray(byte b[])
    {
        if (b.length<=10) emitByteArray(b);
        else
        {
            emitByteArray(Arrays.copyOfRange(b,0,3));
            emit("... ");
            emitByteArray(Arrays.copyOfRange(b,b.length-3,b.length));
        }
    }

    void printByteArray(byte b[])
    {
        for (int i = 0; i < b.length; ++i)
    	{
            System.out.printf("%02x ", b[i]);
    	}
    	System.out.println();
    }

    public static void main(String argv[]) throws Exception
    {
        new A();
    }
}
