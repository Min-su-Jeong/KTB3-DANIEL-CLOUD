package production.simulator.products;

/**
 * 스마트폰 제품 클래스
 */
public class Smartphone extends Electronics {
    private final String model;

    public Smartphone(int id, String name, String brand) {
        super(id, name, brand);
        this.model = brand;
    }
    
    @Override
    public void process(LogSink logger) throws InterruptedException {
        if (logger != null) logger.log(step("스마트폰 조립"));
        Thread.sleep(1500);
        
        provisionSoftware(logger);
        calibrateElectronics(logger);
        electricalSafetyTest(logger);
    }
}