package production.simulator.products;

import java.time.LocalDateTime;

/**
 * 모든 제품의 공통 클래스
 */
public abstract class Product {
    private int id;
    private String name;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String brand;
    private int stepCounter = 0;

    public Product(int id, String name, String brand) {
        if (id <= 0) {
            throw new IllegalArgumentException("제품 ID는 0보다 커야 합니다: " + id);
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("제품명은 비어있을 수 없습니다.");
        }
        if (brand == null || brand.trim().isEmpty()) {
            throw new IllegalArgumentException("브랜드는 비어있을 수 없습니다.");
        }
        
        this.id = id;
        this.name = name.trim();
        this.brand = brand.trim();
        this.status = "PENDING";
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getStatus() { return status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }

    public void setStatus(String status) { 
        if (status == null) {
            throw new IllegalArgumentException("상태는 null일 수 없습니다.");
        }
        this.status = status; 
    }

    // 생산 시작 상태로 변경
    public void markRunning() {
        this.status = "RUNNING";
        this.startedAt = LocalDateTime.now();
        this.stepCounter = 0;
    }

    // 생산 완료 상태로 변경
    public void markDone(LogSink logger) {
        this.status = "DONE";
        this.finishedAt = LocalDateTime.now();
        if (logger != null) {
            logger.log(String.format("[%s] 생산 완료 ✅", Thread.currentThread().getName()));
        }
    }

    // 생산 단계별 로그 메시지 생성
    public String step(String text) {
        stepCounter++;
        return String.format("[%s][%s] 단계 %d: %s", Thread.currentThread().getName(), this.getClass().getSimpleName(), stepCounter, text);
    }
    
    // 제품별 생산 프로세스 실행
    public abstract void process(LogSink logger) throws InterruptedException;

    public interface LogSink {
        void log(String message);
    }
}