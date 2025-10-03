package production.simulator;

import production.simulator.products.*;
import java.util.*;

/**
 * 메뉴를 띄워 제품 등록, 생산 실행, 결과 조회를 처리하는 역할을 하는 메인 클래스
 */
public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final Simulation simulation = new Simulation();
    private int nextId = 100;

    private void registerProductsMenu() {
        System.out.println("생산할 제품 유형을 선택하세요:");
        System.out.println("=============");
        System.out.println("1. Smartphone");
        System.out.println("2. Perfume");
        System.out.println("3. Bed");
        System.out.println("=============");
        System.out.print("> ");

        // 제품 유형 선택
        String typeSel = input("제품 유형을 선택해주세요 (1-3): ");
        String[] types = {"", "Smartphone", "Perfume", "Bed"};
        if (!typeSel.matches("[1-3]")) {
            System.out.println("잘못 입력하셨습니다. 제품 유형을 다시 입력해주세요.");
            return;
        }
        String typeName = types[Integer.parseInt(typeSel)];

        // 수량 입력
        int qty;
        System.out.print("생산할 제품의 수량을 입력하세요: ");
        while (true) {
            try {
                qty = Integer.parseInt(input(""));
                if (qty > 0) break;
                System.out.print("1 이상의 숫자를 입력해주세요: ");
            } catch (NumberFormatException e) {
                System.out.print("잘못 입력하셨습니다. 수량을 다시 입력해주세요: ");
            }
        }

        // 제품 생성 및 등록
        List<Product> newProducts = new ArrayList<>();
        for (int i = 0; i < qty; i++) {
            System.out.printf("%d번째 제품명을 입력하세요: ", i + 1);
            String name = input("제품명을 입력해주세요: ");
            if (name.isEmpty()) {
                System.out.println("잘못 입력하셨습니다. 제품명을 다시 입력해주세요.");
                i--; continue;
            }

            try {
                Product product = createProduct(typeName, nextId++, name);
                if (product != null) {
                    newProducts.add(product);
                } else {
                    i--; nextId--;
                }
            } catch (Exception e) {
                System.out.println("제품 생성 중 오류: " + e.getMessage());
                i--; nextId--;
            }
        }

        // 등록 결과 처리
        if (!newProducts.isEmpty()) {
            simulation.registerProducts(typeName, newProducts);
            System.out.printf("%d건이 등록되었습니다.%n", newProducts.size());
        } else {
            System.out.println("등록된 제품이 없습니다.");
        }
    }

    private void listProducts() {
        List<Product> items = simulation.getProducts().stream()
                .filter(p -> "DONE".equals(p.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        
        if (items.isEmpty()) {
            System.out.println("생산 완료된 제품이 없습니다.");
            return;
        }
        
        System.out.println("생산 완료된 제품 목록입니다:\n");
        printProductsSummary(items);
        printProductsTable(items, true);
    }

    private void startProduction() {
        if (simulation.getProducts().isEmpty()) {
            System.out.println("등록된 제품이 없습니다. 먼저 제품을 등록해 주세요.");
            return;
        }

        System.out.println("생산 대기 중인 제품 목록입니다:\n");
        List<Product> items = simulation.getProducts().stream()
                .filter(p -> "PENDING".equals(p.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        
        if (items.isEmpty()) {
            System.out.println("생산 대기 중인 제품이 없습니다.");
            return;
        }
        
        printProductsSummary(items);
        printProductsTable(items, false);
        System.out.print("\n생산을 시작하시겠습니까? (y/n): ");

        String answer = input("생산을 시작하시겠습니까? (y/n): ").toLowerCase(Locale.ROOT);
        if (answer.matches("y|yes|ㅛ|네|예")) {
            // 생산 라인 수 입력
            int productionLines = inputProductionLines();
            if (productionLines > 0) {
                try {
                    simulation.start(productionLines);
                } catch (Exception e) {
                    System.out.println("생산 중 오류: " + e.getMessage());
                }
            }
        } else {
            System.out.println("생산을 취소합니다.");
        }
    }

    private int inputProductionLines() {
        while (true) {
            try {
                System.out.print("사용할 생산 라인 수를 입력하세요 (1-5): ");
                int lines = Integer.parseInt(input(""));
                if (lines >= 1 && lines <= 5) {
                    return lines;
                }
                System.out.print("1-5 사이의 숫자를 입력해주세요: ");
            } catch (NumberFormatException e) {
                System.out.print("잘못 입력하셨습니다. 생산 라인 수를 다시 입력해주세요: ");
            }
        }
    }

    private void printProductsSummary(List<Product> items) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Product p : items) {
            String type = p.getClass().getSimpleName();
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }
        StringBuilder dist = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (!first) dist.append(", ");
            dist.append(e.getKey()).append(": ").append(e.getValue());
            first = false;
        }
        System.out.printf("총 %d건 (유형별: %s)%n%n", items.size(), dist);
    }

    private void printProductsTable(List<Product> items, boolean showTime) {
        String sep, header, rowFormat;
        
        if (showTime) {
            // 시간 정보 포함 테이블
            sep = "+-------+-----------------+------------------+-----------------------+------------------+------------------+";
            header = "| %-5s | %-14s | %-15s | %-20s | %-14s | %-14s |%n";
            rowFormat = "| %-5s | %-15s | %-16s | %-20s | %-16s | %-16s %n";
            System.out.println(sep);
            System.out.printf(header, "ID", "유형", "이름", "추가 정보", "시작 시간", "종료 시간");
        } else {
            // 기본 테이블
            sep = "+-------+-----------------+------------------+-----------------------+";
            header = "| %-5s | %-14s | %-15s | %-20s |%n";
            rowFormat = "| %-5s | %-15s | %-16s | %-20s %n";
            System.out.println(sep);
            System.out.printf(header, "ID", "유형", "이름", "추가 정보");
        }
        
        System.out.println(sep);
        
        for (Product p : items) {
            String type = p.getClass().getSimpleName();
            String name = p.getName();
            String additionalInfo = getAdditionalInfo(p);
            
            if (showTime) {
                String startTime = formatTime(p.getStartedAt());
                String endTime = formatTime(p.getFinishedAt());
                System.out.printf(rowFormat, p.getId(), type, name, additionalInfo, startTime, endTime);
            } else {
                System.out.printf(rowFormat, p.getId(), type, name, additionalInfo);
            }
        }
        System.out.println(sep);
    }
    
    private String formatTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    private String getAdditionalInfo(Product p) {
        if (p instanceof Smartphone) {
            Smartphone phone = (Smartphone) p;
            return "브랜드: " + phone.getBrand();
        } else if (p instanceof Perfume) {
            Perfume perfume = (Perfume) p;
            return "향: " + perfume.getScent() + ", 용량(mL): " + perfume.getPackageVolume();
        } else if (p instanceof Bed) {
            Bed bed = (Bed) p;
            return "매트리스: " + bed.getMattressSize();
        }
        return "";
    }

    private String input(String prompt) {
        while (true) {
            try {
                String userInput = scanner.nextLine().trim();
                if (!userInput.isEmpty()) return userInput;
                System.out.print("다시 입력해주세요: ");
            } catch (Exception e) {
                System.out.println("잘못 입력하셨습니다. 입력값을 다시 입력해주세요.");
                System.out.print(prompt);
            }
        }
    }
    
    private Product createProduct(String typeName, int id, String name) {
        try {
            switch (typeName) {
                case "Smartphone":
                    System.out.print("브랜드: ");
                    String brand = input("브랜드를 입력하세요: ");
                    return new Smartphone(id, name, brand);
                    
                case "Perfume":
                    System.out.print("브랜드: ");
                    String perfumeBrand = input("브랜드를 입력하세요: ");
                    System.out.print("향: ");
                    String scent = input("향을 입력하세요: ");
                    System.out.print("용량(예: 50ml): ");
                    String volume = input("용량을 입력하세요: ");
                    return new Perfume(id, name, perfumeBrand, volume, scent);
                    
                case "Bed":
                    System.out.print("브랜드: ");
                    String bedBrand = input("브랜드를 입력하세요: ");
                    System.out.print("매트리스 크기(예: Q, K, S): ");
                    String mattressSize = input("매트리스 크기를 입력하세요: ");
                    return new Bed(id, name, bedBrand, mattressSize);
                    
                default:
                    System.out.println("알 수 없는 제품 유형: " + typeName);
                    return null;
            }
        } catch (Exception e) {
            System.out.println("제품 생성 중 오류가 발생했습니다: " + e.getMessage());
            return null;
        }
    }

    private void pause() {
        System.out.println("\n엔터를 누르면 메뉴로 돌아갑니다...");
        try {
            scanner.nextLine();
        } catch (Exception e) {
            // 입력 오류 시 무시하고 계속 진행하기
        }
    }

    private void run() {
        while (true) {
            try {
                // 메인 메뉴 표시
                System.out.println("================");
                System.out.println("제품 생산 시뮬레이션");
                System.out.println("================\n");
                System.out.println("1. 생산 제품 등록");
                System.out.println("2. 생산 시작");
                System.out.println("3. 생산된 제품 목록 보기");
                System.out.println("0. 종료");
                System.out.print("> ");

                String sel = input("메뉴를 선택하세요 (0-3): ");
                if (!sel.matches("[0-3]")) {
                    System.out.println("잘못 입력하셨습니다. 메뉴를 다시 선택해주세요.");
                    continue;
                }
                
                switch (sel) {
                    case "1": registerProductsMenu(); break;
                    case "2": startProduction(); pause(); break;
                    case "3": listProducts(); pause(); break;
                    case "0": System.out.println("프로그램을 종료합니다."); return;
                }
            } catch (Exception e) {
                System.out.println("오류 발생: " + e.getMessage());
                pause();
            }
        }
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }
}