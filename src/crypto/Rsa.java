package crypto;

import java.math.BigInteger;


public class Rsa {
	public static void main(String[] args) {
		Rsa.test_bigint();
	}

	public static void test_bigint() {
		BigInteger bigint = new BigInteger("1234".getBytes());
		BigInteger bigint2 = new BigInteger("1234".getBytes());
		System.out.print(bigint.add(bigint2));
	}
}