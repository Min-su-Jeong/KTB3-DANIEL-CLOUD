package production.simulator;

import production.simulator.products.Product;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * 등록된 제품들을 스레드로 병렬처리 및 로그 기록을 위한 클래스
 */
public class Simulation implements Product.LogSink {
    private final List<Product> products = Collections.synchronizedList(new ArrayList<>());
    private ExecutorService executorService;
    private volatile boolean running = false;

    private static String nowStr() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public void registerProducts(String type, List<Product> newProducts) {
        if (newProducts == null || newProducts.isEmpty()) {
            throw new IllegalArgumentException("등록할 제품 목록이 비어있습니다.");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("제품 유형이 지정되지 않았습니다.");
        }
        
        try {
            products.addAll(newProducts);
        } catch (Exception e) {
            throw new RuntimeException("제품 등록 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public synchronized void start(int productionLines) {
        if (running) {
            System.out.println("이미 생산이 진행 중입니다.");
            return;
        }
        
        if (products.isEmpty()) {
            throw new IllegalStateException("등록된 제품이 없습니다. 먼저 제품을 등록해주세요.");
        }
        
        // 실제 생산할 제품 수 계산
        long pendingCount = products.stream()
                .filter(p -> "PENDING".equals(p.getStatus()))
                .count();
        
        if (pendingCount == 0) {
            System.out.println("생산할 제품이 없습니다.");
            return;
        }
        
        running = true;
        System.out.printf("[%s] 생산 시작: 총 %d건, 생산 라인 %d개%n%n", nowStr(), pendingCount, productionLines);
        
        try {
            executorService = Executors.newFixedThreadPool(productionLines);
            
            // 제품들을 스레드 풀에 제출
            List<Future<?>> futures = new ArrayList<>();
            for (Product p : products) {
                if (!"PENDING".equals(p.getStatus())) continue;
                
                Future<?> future = executorService.submit(() -> runProduct(p));
                futures.add(future);
            }
            
            // 모든 작업 완료 대기
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    System.out.println("스레드 대기 중 인터럽트가 발생했습니다: " + e.getMessage());
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    System.out.println("작업 실행 중 오류: " + e.getCause().getMessage());
                }
            }
            
            // 결과 통계 출력
            long success = futures.stream()
                    .mapToLong(future -> {
                        try {
                            future.get();
                            return 1; // 성공
                        } catch (Exception e) {
                            return 0; // 실패
                        }
                    })
                    .sum();
            long fail = futures.size() - success;
            System.out.printf("%n[%s] 전체 완료: 성공=%d, 실패=%d%n", nowStr(), success, fail);
        } catch (Exception e) {
            System.out.println("생산 중 예상치 못한 오류가 발생했습니다: " + e.getMessage());
            throw new RuntimeException("생산 실행 실패", e);
        } finally {
            running = false;
            if (executorService != null) {
                executorService.shutdown();
            }
        }
    }

    private void runProduct(Product product) {
        if (product == null) {
            log(String.format("[%s] 오류: null 제품 처리 시도", Thread.currentThread().getName()));
            return;
        }
        
        try {
            log(String.format("[%s][%s] 생산 시작", Thread.currentThread().getName(), product.getClass().getSimpleName()));
            product.markRunning();
            product.process(this);
            product.markDone(this);
            
        } catch (InterruptedException e) {
            log(String.format("[%s] 생산 중단됨: %s", Thread.currentThread().getName(), e.getMessage()));
            product.setStatus("INTERRUPTED");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log(String.format("[%s] 생산 실패: %s", Thread.currentThread().getName(), e.getMessage()));
            product.setStatus("FAILED");
        }
    }

    public void stop() { 
        running = false;
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }

    @Override
    public void log(String message) {
        if (message == null) {
            message = "null 메시지";
        }
        try {
            String withTime = String.format("[%s] %s", nowStr(), message);
            System.out.println(withTime);
        } catch (Exception e) {
            System.err.println("로그 출력 중 오류 발생: " + e.getMessage());
        }
    }
}