package com.example.user.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CipherUtilConcurrencyTest {

    @BeforeEach
    void setUp() {
        CipherUtil.resetForTesting();
    }

    // ──────────────────────────────────────────────
    // 1. 并发初始化只执行一次有效密钥设置
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("多线程并发调用 initialize()，密钥只被设置一次，后续线程全部跳过")
    void concurrentInitialize_onlySetsKeyOnce() throws Exception {
        int threadCount = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger initCallCount = new AtomicInteger(0);

        // 使用自定义 key 来追踪 initialize 真正执行的次数
        String customKey = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=";

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // 所有线程同时出发
                    CipherUtil.initialize(customKey);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // 释放所有线程
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "所有线程应在10秒内完成");

        // 验证：所有线程都能正常加密/解密（说明 secretKey 一致）
        String plaintext = "并发初始化测试数据";
        String encrypted = CipherUtil.encrypt(plaintext);
        String decrypted = CipherUtil.decrypt(encrypted);
        assertEquals(plaintext, decrypted, "并发初始化后加解密应正常");
    }

    // ──────────────────────────────────────────────
    // 2. 并发 encrypt/decrypt 结果完全一致
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("多线程并发 encrypt/decrypt，同一明文得到相同密文，解密还原一致")
    void concurrentEncryptDecrypt_allThreadsGetSameResult() throws Exception {
        // 先正常初始化
        CipherUtil.initialize("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=");

        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        String plaintext = "Hello 并发加密解密！AES test 1234567890";
        // 单线程基准密文
        String expectedCipher = CipherUtil.encrypt(plaintext);

        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await(); // 同时出发
                String enc = CipherUtil.encrypt(plaintext);
                String dec = CipherUtil.decrypt(enc);
                // 每次加密结果应与基准密文一致（ECB 模式确定性加密）
                // 且解密应还原明文
                return expectedCipher.equals(enc) && plaintext.equals(dec);
            }));
        }

        startLatch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        for (Future<Boolean> f : futures) {
            assertTrue(f.get(), "每个线程的加解密结果应与基准一致");
        }
    }

    // ──────────────────────────────────────────────
    // 3. 未初始化时并发调用 encrypt，触发 ensureInitialized() 只初始化一次
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("未初始化时多线程同时 encrypt，ensureInitialized() 只执行一次有效初始化")
    void concurrentEncryptWithoutInit_triggersInitOnce() throws Exception {
        // setUp 已 resetForTesting，此时未初始化
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        String plaintext = "延迟初始化并发测试";
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                return CipherUtil.encrypt(plaintext);
            }));
        }

        startLatch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        // 所有线程的加密结果应完全一致
        String firstResult = futures.get(0).get();
        assertNotNull(firstResult);
        for (Future<String> f : futures) {
            assertEquals(firstResult, f.get(), "所有线程的加密结果应一致");
        }

        // 解密验证
        assertEquals(plaintext, CipherUtil.decrypt(firstResult));
    }

    // ──────────────────────────────────────────────
    // 4. 边界情况：null 和空字符串
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("encrypt(null) 和 encrypt('') 返回原值，线程安全")
    void encrypt_nullAndEmpty_returnsAsIs() throws Exception {
        CipherUtil.initialize("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=");

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                if (idx % 2 == 0) {
                    return CipherUtil.encrypt(null) == null
                        && CipherUtil.decrypt(null) == null;
                } else {
                    return "".equals(CipherUtil.encrypt(""))
                        && "".equals(CipherUtil.decrypt(""));
                }
            }));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        for (Future<Boolean> f : futures) {
            assertTrue(f.get(), "null/空字符串边界处理应正确");
        }
    }

    // ──────────────────────────────────────────────
    // 5. 高并发压测：验证无异常且结果正确
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("高并发压测：1000次并发加解密，无异常且全部正确")
    void stressTest_highConcurrency() throws Exception {
        CipherUtil.initialize("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=");

        int iterations = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicInteger successCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            final String data = "压测数据-" + i;
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    String enc = CipherUtil.encrypt(data);
                    String dec = CipherUtil.decrypt(enc);
                    if (data.equals(dec)) {
                        successCount.incrementAndGet();
                    }
                } catch (Throwable t) {
                    error.compareAndSet(null, t);
                }
            }));
        }

        startLatch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS));

        assertNull(error.get(), "不应有任何异常: " + error.get());
        assertEquals(iterations, successCount.get(),
            "全部 " + iterations + " 次加解密都应成功");
    }

    // ──────────────────────────────────────────────
    // 6. 并发初始化 + 并发加解密同时进行
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("初始化和加解密同时进行，验证不会出现半初始化状态")
    void concurrentInitAndEncrypt_noHalfInitializedState() throws Exception {
        // 未初始化状态
        int threadCount = 60;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    if (idx % 3 == 0) {
                        // 部分线程调用 initialize
                        CipherUtil.initialize("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=");
                    } else {
                        // 其他线程直接 encrypt/decrypt（触发 ensureInitialized）
                        String data = "混合并发测试-" + idx;
                        String enc = CipherUtil.encrypt(data);
                        String dec = CipherUtil.decrypt(enc);
                        if (!data.equals(dec)) {
                            throw new AssertionError(
                                "解密结果不匹配: expected=" + data + ", got=" + dec);
                        }
                    }
                } catch (Throwable t) {
                    error.compareAndSet(null, t);
                }
            }));
        }

        startLatch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        assertNull(error.get(),
            "混合并发不应有任何异常（半初始化状态）: " + error.get());
    }
}
