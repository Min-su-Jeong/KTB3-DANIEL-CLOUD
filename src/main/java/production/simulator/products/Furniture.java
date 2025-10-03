package production.simulator.products;

/**
 * 가구 제품 공통 클래스
 */
public class Furniture extends Product {
    public Furniture(int id, String name, String brand) {
        super(id, name, brand);
    }
    
    // 가구 제품 생산 단계들
    public void cutAndAssemble(LogSink logger) throws InterruptedException {
        if (logger != null) logger.log(step("재단 및 조립"));
        Thread.sleep(1200);
    }

    public void finishSurface(LogSink logger) throws InterruptedException {
        if (logger != null) logger.log(step("표면 마감"));
        Thread.sleep(900);
    }

    public void stabilityCheck(LogSink logger) throws InterruptedException {
        if (logger != null) logger.log(step("안정성 검사"));
        Thread.sleep(800);
    }

    @Override
    public void process(LogSink logger) throws InterruptedException {
        cutAndAssemble(logger);
        finishSurface(logger);
        stabilityCheck(logger);
    }
}


