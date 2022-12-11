package com.greencloud.commons.utils;

public class CommonUtils {
	/**
	 * @param n input number
	 * @return true if the number is in a Fibonacci sequence
	 */
	public static boolean isFibonacci(int n) {
		return isPerfectSquare(5 * n * n + 4) ||
				isPerfectSquare(5 * n * n - 4);
	}

	static boolean isPerfectSquare(int x) {
		int s = (int) Math.sqrt(x);
		return (s * s == x);
	}
}
