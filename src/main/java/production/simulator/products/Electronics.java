package production.simulator.products;

/**
 * 전자 제품 공통 클래스
 */
public class Electronics extends Product {
    public Electronics(int id, String name, String brand) {
        super(id, name, brand);
    }

    // 전자제품 생산 단계들
    public void provisionSoftware(LogSink logger) throws InterruptedException {
        if (logger != null) logger.log(step("OS/펌웨어 설치"));
        Thread.sleep(1200);
    }

    public void calibrateElectronics(LogSink logger) throws InterruptedException {
        if (logger != null) logger.log(step("보정"));
        Thread.sleep(800);
    }

    public void electricalSafetyTest(LogSink logger) throws InterruptedException {
        if (logger != null) logger.log(step("기능/안전 테스트"));
        Thread.sleep(1000);
    }

    @Override
    public void process(LogSink logger) throws InterruptedException {
        provisionSoftware(logger);
        calibrateElectronics(logger);
        electricalSafetyTest(logger);
    }
}


