package phpor.crypto;

import java.math.BigInteger;

/**
 * 1. modPow(...) 是在rsa计算中用到的
 */
public class Rsa {
	public static void main(String[] args) {
		Rsa.test_bigint();
	}

	public static void test_bigint() {
		long l = 1234;
		BigInteger bigint = BigInteger.valueOf(l);
		BigInteger bigint2 = BigInteger.valueOf(l);
		BigInteger o = bigint.add(bigint2);
        byte[] b = {-1, 2};
        System.out.println(new BigInteger(b));
		byte[] a = o.toByteArray();
		for (int i = 0; i < a.length; i++ ) {
			System.out.print(a[i]);
			System.out.println();
		}
	}
}
