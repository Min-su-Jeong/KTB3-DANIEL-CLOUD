package production.simulator.products;

/**
 * 침대 제품 클래스
 */
public class Bed extends Furniture {
    private final String mattressSize;

    public Bed(int id, String name, String brand, String mattressSize) {
        super(id, name, brand);
        if (mattressSize == null || mattressSize.trim().isEmpty()) {
            throw new IllegalArgumentException("매트리스 크기는 비어있을 수 없습니다.");
        }
        this.mattressSize = mattressSize.trim();
    }

    public String getMattressSize() { return mattressSize; }
    
    @Override
    public void process(LogSink logger) throws InterruptedException {
        cutAndAssemble(logger);
        finishSurface(logger);
        stabilityCheck(logger);
    }
}