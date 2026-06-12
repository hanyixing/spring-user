package com.example.user.util;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CipherUtil 线程安全与功能一致性测试。
 *
 * <p>重点验证：
 * <ul>
 *   <li>修复竞态条件后，多线程并发调用 encrypt/decrypt 不会因半初始化（secretKey 为 null）抛异常，
 *       也不会因密钥被覆盖导致解密结果错误；</li>
 *   <li>初始化只生效一次（首次 initialize 的密钥胜出，后续调用为空操作）；</li>
 *   <li>加解密功能、默认密钥、算法与修复前完全一致；</li>
 *   <li>边界情况（null、空字符串）按原语义原样返回。</li>
 * </ul>
 *
 * <p>由于 CipherUtil 使用静态状态，测试通过反射在每个场景开始前把 {@code initialized}/{@code secretKey}
 * 复位为未初始化，从而真实地复现“冷启动”时的初始化竞态。
 */
class CipherUtilTest {

    /** 与 CipherUtil.DEFAULT_KEY 保持一致，用于基准（golden）比对，确保默认密钥未被改动。 */
    private static final String DEFAULT_KEY_B64 = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=";

    /** 16 字节 AES-128 密钥 A（运行时编码，避免手写 base64 出错）。 */
    private static final String KEY_A =
            Base64.getEncoder().encodeToString("0123456789abcdef".getBytes(StandardCharsets.UTF_8));
    /** 16 字节 AES-128 密钥 B，与 A 不同，用于验证“首次初始化生效”。 */
    private static final String KEY_B =
            Base64.getEncoder().encodeToString("ABCDEFGHIJKLMNOP".getBytes(StandardCharsets.UTF_8));

    /** 通过反射把 CipherUtil 的静态状态复位为未初始化，使后续调用重新走初始化逻辑。 */
    private static void resetState() throws Exception {
        Field initialized = CipherUtil.class.getDeclaredField("initialized");
        initialized.setAccessible(true);
        initialized.setBoolean(null, false);

        Field secretKey = CipherUtil.class.getDeclaredField("secretKey");
        secretKey.setAccessible(true);
        secretKey.set(null, null);
    }

    // ---------------------------------------------------------------------
    // 功能与边界
    // ---------------------------------------------------------------------

    @Test
    void encryptThenDecrypt_returnsOriginal() throws Exception {
        resetState();
        String plain = "Hello, 世界! 123 @#$";
        String cipherText = CipherUtil.encrypt(plain);

        assertNotNull(cipherText);
        assertNotEquals(plain, cipherText, "密文不应等于明文");
        assertEquals(plain, CipherUtil.decrypt(cipherText), "解密结果应还原明文");
    }

    @Test
    void encrypt_isDeterministic_withFixedKey() throws Exception {
        resetState();
        String plain = "deterministic-input-相同输入";
        // AES/ECB + 固定密钥是确定性的：相同明文必产生相同密文
        assertEquals(CipherUtil.encrypt(plain), CipherUtil.encrypt(plain));
    }

    @Test
    void nullAndEmpty_areReturnedAsIs() throws Exception {
        resetState();
        // 与原实现一致：null/空串在初始化之前短路返回，原样返回
        assertNull(CipherUtil.encrypt(null));
        assertNull(CipherUtil.decrypt(null));
        assertEquals("", CipherUtil.encrypt(""));
        assertEquals("", CipherUtil.decrypt(""));
    }

    /**
     * 基准比对：未显式 initialize 时，懒加载必须使用默认密钥 + AES/ECB/PKCS5Padding，
     * 其输出需与独立实现逐字节一致，证明加密功能/默认密钥/算法均未因本次修复而改变。
     */
    @Test
    void lazyInit_usesDefaultKeyAndAlgorithm_unchanged() throws Exception {
        resetState();
        String plain = "golden-检查-Test@123";
        String actual = CipherUtil.encrypt(plain); // 触发懒加载默认密钥

        byte[] keyBytes = Base64.getDecoder().decode(DEFAULT_KEY_B64);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
        String expected = Base64.getEncoder()
                .encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));

        assertEquals(expected, actual, "懒加载默认密钥/算法的输出与基准不一致，功能发生了改变");
    }

    // ---------------------------------------------------------------------
    // 线程安全
    // ---------------------------------------------------------------------

    /**
     * 验证初始化只生效一次：先用 KEY_A 初始化，再用大量线程并发调用 initialize(KEY_B)；
     * 由于“首次初始化胜出”，最终生效的密钥仍必须是 KEY_A（KEY_B 全部为空操作）。
     */
    @Test
    void initialize_isIdempotent_firstKeyWins_underConcurrency() throws Exception {
        String probe = "race-probe-2026";

        // 前提校验：两个密钥应产生不同密文，否则本测试无意义
        resetState();
        CipherUtil.initialize(KEY_A);
        String underA = CipherUtil.encrypt(probe);
        resetState();
        CipherUtil.initialize(KEY_B);
        String underB = CipherUtil.encrypt(probe);
        assertNotEquals(underA, underB, "测试前提：KEY_A 与 KEY_B 应产生不同密文");

        // 实测：先 A 初始化，再并发 B 初始化
        resetState();
        CipherUtil.initialize(KEY_A);

        int threads = 64;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            for (int i = 0; i < threads; i++) {
                pool.execute(() -> {
                    try {
                        start.await();
                        CipherUtil.initialize(KEY_B);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown(); // 同时放行，最大化竞态窗口
            assertTrue(done.await(10, TimeUnit.SECONDS), "并发初始化超时");
        } finally {
            pool.shutdownNow();
        }

        assertEquals(underA, CipherUtil.encrypt(probe),
                "首次初始化的密钥(KEY_A)被并发覆盖，说明初始化未保持唯一");
    }

    /**
     * 核心竞态回归：每轮从“冷启动”（未初始化）状态出发，大量线程同时进行 encrypt→decrypt 往返。
     *
     * <p>修复前可能出现：
     * <ul>
     *   <li>读到 initialized=true 但 secretKey 仍为 null → NullPointerException；</li>
     *   <li>初始化过程中 secretKey 被并发覆盖 → 解密结果错误。</li>
     * </ul>
     * 修复后所有线程都应往返成功且结果正确。多轮迭代用于提高竞态暴露概率。
     */
    @Test
    void concurrentEncryptDecrypt_isConsistent_fromColdStart() throws Exception {
        int iterations = 30;
        int threads = 64;

        for (int iter = 0; iter < iterations; iter++) {
            resetState(); // 每轮冷启动，强制触发初始化竞态

            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
            AtomicInteger okCount = new AtomicInteger();
            final int round = iter;

            try {
                for (int t = 0; t < threads; t++) {
                    final int id = t;
                    pool.execute(() -> {
                        try {
                            start.await();
                            String plain = "user-" + id + "-密码@" + round;
                            String cipherText = CipherUtil.encrypt(plain);
                            String back = CipherUtil.decrypt(cipherText);
                            if (plain.equals(back)) {
                                okCount.incrementAndGet();
                            } else {
                                errors.add(new AssertionError(
                                        "往返不一致: expected=" + plain + " got=" + back));
                            }
                        } catch (Throwable e) {
                            errors.add(e); // 捕获 NPE 等并发异常
                        } finally {
                            done.countDown();
                        }
                    });
                }
                start.countDown();
                assertTrue(done.await(15, TimeUnit.SECONDS), "并发任务超时, iter=" + iter);
            } finally {
                pool.shutdownNow();
            }

            assertTrue(errors.isEmpty(),
                    "并发加解密出现异常或错误结果 (iter=" + iter + "): " + errors);
            assertEquals(threads, okCount.get(),
                    "部分线程加解密失败 (iter=" + iter + ")");
        }
    }
}
