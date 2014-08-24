package phpor.crypto;

import java.io.IOException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class LangUtils
{
  public static String base64Encode(byte[] bstr)
  {
    return new BASE64Encoder().encode(bstr);
  }

  public static byte[] base64Decode(String str)
  {
    byte[] bt = (byte[])null;
    try {
      BASE64Decoder decoder = new BASE64Decoder();
      bt = decoder.decodeBuffer(str);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return bt;
  }

  public static byte[] addArrayAll(byte[] array1, byte[] array2)
  {
    if (array1 == null)
      return clone(array2);
    if (array2 == null) {
      return clone(array1);
    }
    byte[] joinedArray = new byte[array1.length + array2.length];
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    return joinedArray;
  }

  private static byte[] clone(byte[] array) {
    if (array == null) {
      return null;
    }
    return (byte[])array.clone();
  }
}