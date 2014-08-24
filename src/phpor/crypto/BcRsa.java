package phpor.crypto;


import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.Enumeration;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JCERSACipher;
import sun.misc.*;

/**
 * Created by phpor on 15/12/1.
 */
public class BcRsa {
    private static final String JKS = "JKS";
    private static final String P12 = "P12";
    private static final String PKCS12 = "PKCS12";
    private static final String JCEKS = "JCEKS";
    private static final String JCK = "JCK";
    private static final String PFX = "PFX";
    private static String PROVIDER_NAME = null;

    static {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        PROVIDER_NAME = provider.getName();
        Security.addProvider(provider);
    }

    public static byte[] msgDigest(byte[] textBytes, String algorithm) throws TranFailException
    {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(textBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new TranFailException("摘要算法:" + algorithm + " 不存在", e);
        }
        return messageDigest.digest();
    }

    private static PublicKey getPublicKey(String certPath) throws TranFailException
    {
        java.security.cert.Certificate cert = getCert(certPath);
        return cert.getPublicKey();
    }

    private static java.security.cert.Certificate getCert(String certPath) throws TranFailException {
        java.security.cert.Certificate cert = null;
        try {
            InputStream streamCert = new FileInputStream(certPath);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            cert = factory.generateCertificate(streamCert);
        } catch (FileNotFoundException e1) {
            throw new TranFailException("路径:" + certPath + " 下没有找到证书", e1);
        } catch (CertificateException e2) {
            throw new TranFailException("获取证书失败", e2);
        }
        return cert;
    }

    private static PrivateKey getPrivateKey(String keyPath, String passwd) throws TranFailException
    {
        String keySuffix = keyPath.substring(keyPath.lastIndexOf(".") + 1);

        String keyType = "JKS";
        if ((keySuffix == null) || (keySuffix.trim().equals("")))
            keyType = "JKS";
        else {
            keySuffix = keySuffix.trim().toUpperCase();
        }
        if (keySuffix.equals("P12"))
            keyType = "PKCS12";
        else if (keySuffix.equals("PFX"))
            keyType = "PKCS12";
        else if (keySuffix.equals("JCK"))
            keyType = "JCEKS";
        else {
            keyType = "JKS";
        }
        return getPrivateKey(keyPath, passwd, keyType);
    }

    private static PrivateKey getPrivateKey(String keyPath, String passwd, String keyType) throws TranFailException
    {
        PrivateKey key = null;
        try {
            KeyStore ks = KeyStore.getInstance(keyType);
            char[] cPasswd = passwd.toCharArray();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(keyPath);
                ks.load(fis, cPasswd);
                fis.close();
            } finally {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            }
            Enumeration aliasenum = ks.aliases();
            String keyAlias = null;
            while (aliasenum.hasMoreElements()) {
                keyAlias = (String)aliasenum.nextElement();
                key = (PrivateKey)ks.getKey(keyAlias, cPasswd);
                if (key != null) break;
            }
        }
        catch (Exception e) {
            throw new TranFailException("获取私钥失败", e);
        }
        return key;
    }

    public static byte[] sign(PrivateKey priKey, byte[] b) throws TranFailException
    {
        byte[] returnByte = (byte[])null;
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(priKey);
            sig.update(b);
            returnByte = sig.sign();
        } catch (NoSuchAlgorithmException e1) {
            throw new TranFailException("签名算法SHA1withRSA不存在", e1);
        } catch (InvalidKeyException e2) {
            throw new TranFailException("无效私钥", e2);
        } catch (SignatureException e3) {
            throw new TranFailException("使用私钥签名失败", e3);
        }
        return returnByte;
    }

    public static boolean verify(PublicKey pubKey, byte[] orgByte, byte[] signaByte) throws TranFailException
    {
        boolean isVerify = false;
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(pubKey);
            sig.update(orgByte);
            isVerify = sig.verify(signaByte);
        } catch (NoSuchAlgorithmException e1) {
            throw new TranFailException("签名算法SHA1withRSA不存在", e1);
        } catch (InvalidKeyException e2) {
            throw new TranFailException("无效公钥", e2);
        } catch (SignatureException e3) {
            throw new TranFailException("使用公钥签名失败", e3);
        }
        return isVerify;
    }

    public static byte[] keyEncode(Key pubKey, byte[] plainText) throws TranFailException
    {
        byte[] returnByte = (byte[])null;
        try {
            returnByte = doFinalEncode(pubKey, plainText, 1);
        } catch (Exception e) {
            throw new TranFailException("使用公钥加密失败", e);
        }
        return returnByte;
    }

    public static byte[] keyDecode(Key priKey, byte[] encrypText) throws TranFailException
    {
        byte[] returnByte = (byte[])null;
        try {
            returnByte = doFinalDecode(priKey, encrypText, 2);
        } catch (Exception e) {
            throw new TranFailException("使用私钥解密失败", e);
        }
        return returnByte;
    }

    private static byte[] doFinalEncode(Key key, byte[] textBytes, int MODE)
            throws Exception
    {
        Cipher cipher = Cipher.getInstance(key.getAlgorithm(),
                Security.getProvider(PROVIDER_NAME));

        cipher.init(MODE, key);
        int blockSize = 244;

        int textLength = textBytes.length;

        byte[] retBytes = new byte[0];
        int loop = textLength / blockSize;
        int mod = textLength % blockSize;
        if (loop == 0)
        {
            byte[] bytes = new byte[textBytes.length + 1];
            bytes[0] = 1;
            System.arraycopy(textBytes, 0, bytes, 1, textBytes.length);
            return cipher.doFinal(bytes);
        }
        for (int i = 0; i < loop; i++)
        {
            byte[] dstBytes = new byte[blockSize + 1];

            dstBytes[0] = 1;

            System.arraycopy(textBytes, i * blockSize, dstBytes, 1, blockSize);
            byte[] encryBytes = cipher.doFinal(dstBytes);
            retBytes = LangUtils.addArrayAll(retBytes, encryBytes);
        }
        if (mod != 0) {
            int iPos = loop * blockSize;
            int leavingLength = textLength - iPos;

            byte[] dstBytes = new byte[leavingLength + 1];

            dstBytes[0] = 1;
            System.arraycopy(textBytes, iPos, dstBytes, 1, leavingLength);
            byte[] encryBytes = cipher.doFinal(dstBytes);
            retBytes = LangUtils.addArrayAll(retBytes, encryBytes);
        }
        return retBytes;
    }

    public static byte[] doFinalDecode(Key key, byte[] textBytes, int MODE)
            throws Exception
    {
        Cipher cipher = Cipher.getInstance(key.getAlgorithm(),
                Security.getProvider(PROVIDER_NAME));

        cipher.init(MODE, key);
        int blockSize = cipher.getBlockSize();
        int textLength = textBytes.length;

        byte[] retBytes = new byte[0];
        int loop = textLength / blockSize;
        if (loop == 0)
        {
            byte[] decodes = cipher.doFinal(textBytes);
            byte[] bytes = new byte[textBytes.length - 1];
            System.arraycopy(decodes, 1, bytes, 0, decodes.length - 1);
            return bytes;
        }
        for (int i = 0; i < loop; i++) {
            byte[] dstBytes = new byte[blockSize];
            System.arraycopy(textBytes, i * blockSize, dstBytes, 0, blockSize);
            byte[] encryBytes = cipher.doFinal(dstBytes);
            byte[] cutBytes = new byte[encryBytes.length - 1];
            System.arraycopy(encryBytes, 1, cutBytes, 0, encryBytes.length - 1);
            retBytes = LangUtils.addArrayAll(retBytes, cutBytes);
        }
        return retBytes;
    }

    public static String[] encodePaRes(String mercCert, String clientPriKey, String priKeyPwd, String reqStr, String zipType) throws TranFailException
    {
        String[] retStrs = new String[2];
        try {
            PublicKey pubKey = getPublicKey(mercCert);
            PrivateKey priKey = getPrivateKey(clientPriKey, priKeyPwd);

            byte[] reqByte = reqStr.getBytes("UTF-8");

            if ((zipType != null) && (zipType.equals("1"))) {
                reqByte = ZipUtils.compressByte(reqByte);
            }

            byte[] keyBytes = keyEncode(pubKey, reqByte);

            byte[] digestBytes = msgDigest(keyBytes, "MD5");

            byte[] signBytes = sign(priKey, digestBytes);

            String aa = LangUtils.base64Encode(digestBytes);

            retStrs[0] = LangUtils.base64Encode(keyBytes);

            retStrs[1] = LangUtils.base64Encode(signBytes);
        }
        catch (UnsupportedEncodingException e) {
            throw new TranFailException("不被支持的编码种类", e);
        } catch (IOException ex) {
            throw new TranFailException(ex);
        }
        return retStrs;
    }

    public static String decodePaReq(String mercCert, String serCertPath, String priKeyPwd, String reqStr, String signStr, String zipType) throws TranFailException, IOException {
        String respXML = "";
        try {
            reqStr = reqStr.replace(" ", "+");
            signStr = signStr.replace(" ", "+");

            PublicKey pubKey = getPublicKey(mercCert);
            PrivateKey priKey = getPrivateKey(serCertPath, priKeyPwd);

            byte[] paReqEncryp = LangUtils.base64Decode(reqStr);

            byte[] signByte = LangUtils.base64Decode(signStr);

            byte[] orgMsgDigestByte = msgDigest(paReqEncryp, "MD5");

            boolean isVerify = verify(pubKey, orgMsgDigestByte, signByte);
            if (!isVerify) {
                throw new TranFailException("签名校验失败");
            }

            byte[] paReqByte = keyDecode(priKey, paReqEncryp);

            if ((zipType != null) && (zipType.equals("1"))) {

                    paReqByte = ZipUtils.uncompressByte(paReqByte);
            }
            respXML = new String(paReqByte, "UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new TranFailException("不被支持的编码种类", e);
        }
        return respXML;
    }
    public static void main(String[] args) throws TranFailException, IOException {
        String mercCert = "/Users/phpor/Downloads/szfsWS0000000001.crt";
        String serCertPath = "/Users/phpor/Downloads/szfsC00000000Y89.pfx";
        PrivateKey priKey = getPrivateKey(serCertPath, "a1234567");
//	  String zipType = "1";
//        String encrypt = "MABMQdlpHRcs85EXO+x690f4ak8itI8mHvlvn3+3+mdkp2XqrzxI3PeUpiab3o2JxgVDQizgf/XQ8kEZPjGESeA0t79ztGhC0Cgs36YZ7phVsZ0Y+AwoKkbX0tRi8Hn0TrdzGl0W+UpS6//tW/yu9MLG0JY9oTnD4clB1mKnXnF2sgALeGe3xe8gnisQX+fwWChvX1zvO0pejlz6ldPAb8m5TPs3bzhSmuGsrzqPf8HCeDVQL10ME2rvmYAdJIgmTJNLwgmQarW1snGNCRZZ3SOZKJZXp2Qp581z3G72pi0eZWRqJYuP1xwsUGyZ/1wRl/HLE/MH0UB19ON83nbnTw==";
        //byte[] paReqEncryp = LangUtils.base64Decode("eQL/Wo2Yuzz1e/5xr5XxXN5q420JmII0IttH2bgJdSbtJvLjS/vANq56VG9gT2BEcFpaNIwlEq/R4VnuDzbPQJV+mMz/Z4N0Q5yE3q1l7qCkSghEqvIIGrtNMch6tobpR/S9+ktTOzHHqhX4Xy5oyf3SKi2qGLMavN0eHQE7lf61/wQdMax3MbaKfPIU/xD9/jrr3mQ3vCsPp6j1lin2Iiggr3WCyi7eqybo8p0RwHA6zOnOmw/8AM+kgRWtP7HA7x1EvacyMxANz47guoOr96R2GfAzt1Rl1ROi3q9aNGq5/MGfw2o+zxSfVk49hR25rYfMyaGgY4w5644pLIj1jA==");
        byte[] encrypted = LangUtils.base64Decode("EimWaVr6DZyWV/Emu4XL0dLItVjSbGKhDmlcTFRoz7a5YfS2P9jDo9o21iNqz+GX4JD3o+F0zVsEvCJF06oCodRXsKpkI5tYhFJzjVRtXV2MUmTJCvAC1P7Q5n3Cy/zN0kmucwbO3hZkAUP9w0Mg/XTM/GdJfcznka2lYIS//1/nH536vmlb5gEAlL7XqWMj0jViJJUgJjZ7hkGB7Tu7UwjQBPnqNmljiezoywtAUHxFU6PD94gp6P0TH3wJjWiKEplyqA/qODvoe0g7KMkCfJNjupWw8xnZ+pX/wcp2ovQ5G2pHSHD7VImiMCmLfQwgZzKFTe6SV/sqoE9RLJyzZQ==");

//	  String[] str = encodePaRes(mercCert, serCertPath, "a1234567", "<?xml version=\"1.0\" encoding=\"utf-8\"?><SingleAcp xmlns=\"szfs.tws.pay.singleacp\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"szfs.tws.pay.singleacp szfs.tws.biz.pay.singleacp.xsd\"><subdate>20150616</subdate><outid>20150617000001</outid><corpno>C00000000Y89</corpno><transcode>30202</transcode><feeitem>04903</feeitem><acpbankno>710584000001</acpbankno><acpaccno>11014745776009</acpaccno><acpaccname>北京蜜蜂汇金科技有限公司</acpaccname><paybankno>710584000001</paybankno><payaccno>6225880170539341</payaccno><payaccname>付博</payaccname><currency>CNY</currency><paymoney>0.01</paymoney><contractno>C00000000Y896225880170539341</contractno></SingleAcp>", "0");
//	  String ming = str[0];
//	  String sign = str[1];
//	  System.out.println("msgbody="+ming);
//	  System.out.println("-----------------------------------------------------------");
//	  System.out.println("sign="+sign);
	  PublicKey pubKey = getPublicKey(mercCert);
        String plain = "123";
//        byte [] encrypted = keyEncode(pubKey, plain.getBytes());
        byte[] paReqByte = keyDecode(priKey, encrypted);
        String aa = new String(paReqByte, "UTF8");
        System.out.println(aa);
//      if ((zipType != null) && (zipType.equals("1"))) {
//        try {
//          paReqByte = ZipUtils.uncompressByte(paReqByte);
//        } catch (IOException e) {
//          throw new TranFailException("报文体不是合法的GZIP压缩格式", e);
//        }
//      }
//        String respXML = new String(paReqByte, "UTF8");

    }

    private static class TranFailException extends Exception {
        public TranFailException(String s, Throwable e) {
            super(s, e);
        }

        public TranFailException(String str) {
            super(str);
        }

        public TranFailException(IOException ex) {
            super(ex);
        }
    }

}
