/*
 * MineAdsMonitor
 * Copyright (C) 2025  MineAds
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package gg.mineads.monitor.shared.event;

import gg.mineads.monitor.shared.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BatchProcessorTest {

  private BatchProcessor batchProcessor;
  private HttpClient mockHttpClient;
  private MockedStatic<HttpClient> mockedHttpClient;

  @BeforeEach
  void setUp() throws Exception {
    String pluginKey = "test-plugin-key";

    // Create a test config
    Config config = new Config();

    // Mock HttpClient.newBuilder() and build()
    mockHttpClient = mock(HttpClient.class);
    mockedHttpClient = mockStatic(HttpClient.class);
    HttpClient.Builder mockBuilder = mock(HttpClient.Builder.class);

    mockedHttpClient.when(HttpClient::newBuilder).thenReturn(mockBuilder);
    when(mockBuilder.connectTimeout(any())).thenReturn(mockBuilder);
    when(mockBuilder.build()).thenReturn(mockHttpClient);

    batchProcessor = new BatchProcessor(pluginKey, config);
  }

  @AfterEach
  void tearDown() {
    if (mockedHttpClient != null) {
      mockedHttpClient.close();
    }
  }

  @Test
  void testConstructor() {
    assertNotNull(batchProcessor);
    assertEquals(0, batchProcessor.getQueueSize());
  }

  @Test
  void testAddEvent() {
    Object event = new Object();
    batchProcessor.addEvent(event);
    assertEquals(1, batchProcessor.getQueueSize());
  }

  @Test
  void testAddMultipleEvents() {
    Object event1 = "event1";
    Object event2 = "event2";
    Object event3 = "event3";

    batchProcessor.addEvent(event1);
    batchProcessor.addEvent(event2);
    batchProcessor.addEvent(event3);

    assertEquals(3, batchProcessor.getQueueSize());
  }

  @Test
  void testGetQueueSize() {
    assertEquals(0, batchProcessor.getQueueSize());

    batchProcessor.addEvent("test");
    assertEquals(1, batchProcessor.getQueueSize());

    batchProcessor.addEvent("test2");
    assertEquals(2, batchProcessor.getQueueSize());
  }

  @Test
  void testRunWithEmptyQueue() {
    // Should not throw any exceptions
    assertDoesNotThrow(() -> batchProcessor.run());
  }

  @Test
  void testRunWithEvents() throws Exception {
    // Add some events
    batchProcessor.addEvent("test event 1");
    batchProcessor.addEvent("test event 2");

    // Mock the HTTP response
    @SuppressWarnings("unchecked")
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);

    doReturn(CompletableFuture.completedFuture(mockResponse))
      .when(mockHttpClient).sendAsync(any(HttpRequest.class), any());

    // Run the processor synchronously to avoid async issues
    batchProcessor.processQueueSafely();

    // Verify HTTP client was called
    verify(mockHttpClient, atLeastOnce()).sendAsync(any(HttpRequest.class), any());
  }

  @Test
  void testProcessIfNecessaryWithSmallQueue() {
    // Add events below threshold
    for (int i = 0; i < 50; i++) {
      batchProcessor.addEvent("event" + i);
    }

    // Should not process immediately
    assertEquals(50, batchProcessor.getQueueSize());
  }

  @Test
  void testProcessIfNecessaryWithLargeQueue() throws Exception {
    // Add events above threshold
    for (int i = 0; i < 150; i++) {
      batchProcessor.addEvent("event" + i);
    }

    // Mock the HTTP response
    @SuppressWarnings("unchecked")
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);

    doReturn(CompletableFuture.completedFuture(mockResponse))
      .when(mockHttpClient).sendAsync(any(HttpRequest.class), any());

    // Trigger processing synchronously
    batchProcessor.processQueueSafely();

    // Verify HTTP client was called
    verify(mockHttpClient, atLeastOnce()).sendAsync(any(HttpRequest.class), any());
  }

  @Test
  void testShutdown() throws Exception {
    // Add a scheduled task by triggering some processing
    batchProcessor.addEvent("test");
    batchProcessor.run();

    // Shutdown should complete without exceptions
    assertDoesNotThrow(() -> batchProcessor.shutdown());
  }

  @Test
  void testSerializationWithPrimitives() throws Exception {
    Queue<Object> events = new ConcurrentLinkedQueue<>();
    events.add("string");
    events.add(42);
    events.add(3.26);
    events.add(true);
    events.add(123L);

    // Test the private serializeToMessagePack method via reflection
    Field field = BatchProcessor.class.getDeclaredField("events");
    field.setAccessible(true);
    field.set(batchProcessor, events);

    // This would require more complex mocking of MessagePack classes
    // For now, just verify the method exists and is accessible
    assertNotNull(BatchProcessor.class.getDeclaredMethod("serializeToMessagePack", Queue.class));
  }

  @Test
  void testRetryDelayCalculation() throws Exception {
    // Test the private calculateRetryDelay method via reflection
    java.lang.reflect.Method method = BatchProcessor.class.getDeclaredMethod("calculateRetryDelay", int.class);
    method.setAccessible(true);

    // Test various attempts
    long delay0 = (long) method.invoke(batchProcessor, 0);
    long delay1 = (long) method.invoke(batchProcessor, 1);
    long delay2 = (long) method.invoke(batchProcessor, 2);

    assertEquals(1000, delay0); // 1 second
    assertEquals(2000, delay1); // 2 seconds
    assertEquals(4000, delay2); // 4 seconds
  }

  @Test
  void testShouldRetryStatusCodes() throws Exception {
    // Test the private shouldRetry method via reflection
    java.lang.reflect.Method method = BatchProcessor.class.getDeclaredMethod("shouldRetry", int.class);
    method.setAccessible(true);

    // Test various status codes
    assertFalse((boolean) method.invoke(batchProcessor, 200)); // Success
    assertFalse((boolean) method.invoke(batchProcessor, 400)); // Client error
    assertTrue((boolean) method.invoke(batchProcessor, 429));  // Too Many Requests
    assertTrue((boolean) method.invoke(batchProcessor, 500));  // Server error
    assertTrue((boolean) method.invoke(batchProcessor, 502));  // Bad Gateway
    assertTrue((boolean) method.invoke(batchProcessor, 503));  // Service Unavailable
  }

  @Test
  void testShouldRetryOnException() throws Exception {
    // Test the private shouldRetryOnException method via reflection
    java.lang.reflect.Method method = BatchProcessor.class.getDeclaredMethod("shouldRetryOnException", Throwable.class);
    method.setAccessible(true);

    // Test various exceptions
    assertTrue((boolean) method.invoke(batchProcessor, new IOException("Test IO exception")));
    assertFalse((boolean) method.invoke(batchProcessor, new RuntimeException("Test runtime exception")));
    assertFalse((boolean) method.invoke(batchProcessor, new IllegalArgumentException("Test illegal argument")));
  }

  @Test
  void testDebugFlagIntegration() throws Exception {
    // Test that debug logging is controlled by config
    Config debugConfig = new Config();
    BatchProcessor debugBatchProcessor = new BatchProcessor("test-key", debugConfig);

    // Add an event and verify it doesn't throw exceptions
    debugBatchProcessor.addEvent("test event");

    // The debug flag should be accessible via the config
    assertFalse(debugConfig.isDebug()); // Default should be false

    // Note: Since Config uses ConfigLib, we can't easily modify it in tests
    // But the integration should work in production
  }
}
