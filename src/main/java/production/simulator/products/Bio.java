package production.simulator.products;

import java.lang.reflect.Constructor;

/**
 * 바이오 공통 클래스
 */
public class Bio extends Product {
    private final String packageVolume;
    private final String category;

    public Bio(int id, String name, String brand, String packageVolume, String category) {
        super(id, name, brand);
        if (packageVolume == null || packageVolume.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        
        this.packageVolume = packageVolume.trim();
        this.category = category.trim();
    }

    public String getPackageVolume() { return packageVolume; }
    
    // 바이오 제품 생산 단계들
    public void sanitizeMaterials(LogSink logger) throws InterruptedException {
        if (logger != null) logger.log(step("원재료 살균/준비"));
        Thread.sleep(800);
    }

    public void formulateBatch(LogSink logger) throws InterruptedException {
        if (logger != null) logger.log(step("배치 배합"));
        Thread.sleep(1000);
    }

    public void sealAndLabel(LogSink logger) throws InterruptedException {
        if (logger != null) logger.log(step("충전, 밀봉 및 라벨링"));
        Thread.sleep(900);
    }

    @Override
    public void process(LogSink logger) throws InterruptedException {
        sanitizeMaterials(logger);
        formulateBatch(logger);
        sealAndLabel(logger);
    }
}
