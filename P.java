import java.io.*;
import java.util.*;
import org.ietf.jgss.*;

class P
{
    static boolean raw=false;
    static int cutOff=0;

    static ExtendedOid oid=null;

    public static void print(int level,byte asn1Object[]) throws Exception
    {
        if((cutOff>0)&&(level>cutOff))
        {
            return;
        }

        if(raw)
        {
            System.out.println();
            printByteArray(asn1Object);
            System.out.println();
        }

        int tag=Byte.toUnsignedInt(asn1Object[0]);
        int length=getLength(asn1Object);
        int lengthOfLength=getLengthOfLength(asn1Object);
        byte value[]=Arrays.copyOfRange(asn1Object,lengthOfLength+1, length+lengthOfLength+1);

        emitSpacing(level);

        if (tag == 0x01)
        {
            emit("boolean: ");
            emit((value[0]==0x00)?"False":"True");
            emitnl();
        }
        else if (tag == 0x02)
        {
            emit("integer: ");
            emitByteArray(value);
            emitnl();
        }
        else if (tag == 0x03)
        {
            emit("bit string: ");
            emitBitString(value); 
            emitnl();
        }
        else if (tag == 0x04)
        {
            emit("octet string: ");
            emitnl();
            print(level+1,value);
        }
        else if (tag == 0x05)
        {
            emit("null "); 
            emitnl();
        }
        else if (tag == 0x06)
        {
            emit("oid: ");
            oid = new ExtendedOid(asn1Object);
            emit(oid.toString());  
            emitnl();
        }
        else if (tag == 0x0c)
        {
            emit("UTF8String: ");
            emit(new String(value)); 
            emitnl();
        }
        else if (tag == 0x13)
        {
            emit("printable string: ");
            emit(new String(value));
            emitnl();
        }
        else if (tag == 0x16)
        {
            emit("IA5 String: ");
            emit(new String(value)); 
            emitnl();
        }
        else if (tag == 0x17)
        {
            emit("UTCTime: ");
            emit(new String(value));
            emitUTCTime(value); 
            emitnl();
        }
        else if (tag == 0x30)
        {
            emit("sequence");
            emitnl();
            byte first[], rest[];
            rest=Arrays.copyOf(value,value.length);

            while(rest.length>0)
            {
                length=getLength(rest);
                lengthOfLength=getLengthOfLength(rest);
                first=Arrays.copyOfRange(rest,0,length+lengthOfLength+1);
                rest=Arrays.copyOfRange(rest,length+lengthOfLength+1,rest.length);
                print(level+1,first);
            }
        }
        else if (tag == 0x31)
        {
            emit("set: ");
            emitnl();
            byte first[], rest[];
            rest=Arrays.copyOf(value,value.length);
            while(rest.length>0)
            {
                length=getLength(rest);
                lengthOfLength=getLengthOfLength(rest);
                first=Arrays.copyOfRange(rest,0,length+lengthOfLength+1);
                rest=Arrays.copyOfRange(rest,length+lengthOfLength+1,rest.length);
                print(level+1,first);
            }
        }
        else if (((tag&0x80)==0x80)&&((tag&0x40)==0x00))
        {
            if ((tag&0x20)!=0x20)
            {
                emit("primative context-specific: "); 
                System.out.printf("%02x",tag);
                emitnl();
                emitSpacing(level+1);
                emitElidedByteArray(value); 
                emitnl();
            }
            else
            {
                emit("constructed context-specific: "); 
                System.out.printf("%02x",tag);
                emitnl();
                if(value.length>0)print(level+1,value);
            }
        }
        else if ((tag&0x1f)==0x1f)
        {
            System.out.println("Sorry, no support for multibyte tags");
            System.exit(0);
        }
        else 
        {
            System.out.printf("Unknown tag: 0x%02x\n",(tag));
            emitElidedByteArray(value); 
            emitnl();
        }
    }

    public static int getLength(byte asn1Object[]) throws Exception
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
        else if(Byte.toUnsignedInt(asn1Object[1])==0x83) 
        {
            return (256*256*Byte.toUnsignedInt(asn1Object[2]))+
                   (256*Byte.toUnsignedInt(asn1Object[3]))+
                    Byte.toUnsignedInt(asn1Object[4]);
        }
        else { System.out.printf("%02x\n",asn1Object[1]);
throw(new Exception());}
    }

    public static int getLengthOfLength(byte asn1Object[]) throws Exception
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
        else if(Byte.toUnsignedInt(asn1Object[1])==0x83) 
        {
            return 4;
        }
        else throw(new Exception());
    }

    public static void emit(String s)
    {
    	System.out.print(s);
    }

    public static void emitnl()
    {
    	System.out.println();
    }

    public static void emitSpacing(int level)
    {
        for (int i=0;i<level;++i) System.out.print("  ");
    }

    public static void emitByteArray(byte b[])
    {
        for (int i = 0; i < b.length; ++i)
    	{
            System.out.printf("%02x ", b[i]);
    	}
    }

    public static void emitElidedByteArray(byte b[])
    {
        if (b.length<=10) emitByteArray(b);
        else
        {
            emitByteArray(Arrays.copyOfRange(b,0,3));
            emit("... ");
            emitByteArray(Arrays.copyOfRange(b,b.length-3,b.length));
        }
    }

    public static void emitBitString(byte b[])
    {
        int unusedBitCount=Byte.toUnsignedInt(b[0]);
        emitElidedByteArray(Arrays.copyOfRange(b,1,b.length));
        if(unusedBitCount>0)
        {
            emit(String.format(" (ignore last %d bits)",unusedBitCount));
        }
    }

    public static void emitUTCTime(byte b[])
    {
    }

    public static void printByteArray(byte b[])
    {
        for (int i = 0; i < b.length; ++i)
    	{
            System.out.printf("%02x ", b[i]);
    	}
    	System.out.println();
    }
}
