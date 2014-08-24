package phpor.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ZipUtils
{
  private static final int BUFFER = 1024;
  private static final String EXT = ".gz";

  public static void compress(File file)
    throws Exception
  {
    compress(file, true);
  }

  public static void compress(String path, boolean delete)
    throws Exception
  {
    File file = new File(path);
    compress(file, delete);
  }

  public static void compress(File file, boolean delete)
    throws Exception
  {
    FileInputStream fis = new FileInputStream(file);
    FileOutputStream fos = new FileOutputStream(file.getPath() + ".gz");

    compress(fis, fos);

    fis.close();
    fos.flush();
    fos.close();

    if (delete)
      file.delete();
  }

  public static void compress(InputStream is, OutputStream os)
    throws Exception
  {
    GZIPOutputStream gos = new GZIPOutputStream(os);

    byte[] data = new byte[1024];
    int count;
    while ((count = is.read(data, 0, 1024)) != -1)
    {
      gos.write(data, 0, count);
    }

    gos.finish();

    gos.flush();
    gos.close();
  }

  public static String compressStr(String str)
    throws IOException
  {
    if ((str == null) || (str.length() == 0)) {
      return str;
    }
    return compress(str.getBytes()).toString("ISO-8859-1");
  }

  public static byte[] compressByte(byte[] content)
    throws IOException
  {
    return compress(content).toByteArray();
  }

  private static ByteArrayOutputStream compress(byte[] content) throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    GZIPOutputStream gzip = new GZIPOutputStream(out);
    gzip.write(content);
    gzip.close();
    return out;
  }

  public static String uncompressStr(String str)
    throws IOException
  {
    if ((str == null) || (str.length() == 0)) {
      return str;
    }
    return uncompress(str.getBytes("ISO-8859-1")).toString();
  }

  public static byte[] uncompressByte(byte[] content)
    throws IOException
  {
    return uncompress(content).toByteArray();
  }

  public static ByteArrayOutputStream uncompress(byte[] content) throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayInputStream in = new ByteArrayInputStream(content);
    GZIPInputStream gunzip = new GZIPInputStream(in);
    byte[] buffer = new byte[256];
    int n;
    while ((n = gunzip.read(buffer)) >= 0)
    {
      out.write(buffer, 0, n);
    }
    return out;
  }
}