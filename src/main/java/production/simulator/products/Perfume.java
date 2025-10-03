package production.simulator.products;

/**
 * 향수 제품 클래스
 */
public class Perfume extends Bio {
    private final String scent;

    public Perfume(int id, String name, String brand, String volume, String scent) {
        super(id, name, brand, volume, "Cosmetic");
        if (scent == null || scent.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.scent = scent.trim();
    }

    public String getScent() { return scent; }
    
    @Override
    public void process(LogSink logger) throws InterruptedException {
        sanitizeMaterials(logger);
        formulateBatch(logger);
        sealAndLabel(logger);
    }
}