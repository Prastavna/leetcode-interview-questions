package com.prastavna.leetcode.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Uuid {
  private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

  public static String generate(String prefix) {
    UUID uuid = UUID.randomUUID();
    byte[] bytes = asBytes(uuid);
    String base58 = toBase58(bytes);
    return prefix + "_" + base58;
  }

  private static byte[] asBytes(UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return bb.array();
  }

  private static String toBase58(byte[] input) {
    java.math.BigInteger num = new java.math.BigInteger(1, input);
    StringBuilder sb = new StringBuilder();
    while (num.compareTo(java.math.BigInteger.ZERO) > 0) {
      int rem = num.mod(java.math.BigInteger.valueOf(58)).intValue();
      sb.append(ALPHABET[rem]);
      num = num.divide(java.math.BigInteger.valueOf(58));
    }
    return sb.reverse().toString();
  }
}
