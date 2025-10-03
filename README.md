# 주제: 제품 생산 시뮬레이션

멀티스레딩을 활용한 제품 생산 공장 시뮬레이션 프로그램입니다. 다양한 제품을 등록하고 여러 생산 라인에서 병렬로 생산하며 실시간으로 진행 상황을 모니터링할 수 있습니다.


## 기능

- **다양한 제품 등록**: 다양한 제품 유형 지원
- **병렬 생산 처리**: 최대 5개의 생산 라인에서 동시 생산
- **실시간 모니터링**: 스레드별 생산 진행 상황 실시간 출력
- **결과 통계**: 생산 완료된 제품의 상세 결과 조회


## 프로젝트 구조

```
src/main/java/production/simulator/
├── Main.java                    # 메인 애플리케이션 (CLI 인터페이스)
├── Simulation.java              # 생산 시뮬레이션 엔진 (멀티스레딩 관리)
└── products/
    ├── Product.java             # 제품 추상 클래스
    ├── Electronics.java         # 전자제품 공통 클래스 (Product 상속)
    ├── Furniture.java           # 가구 공통 클래스 (Product 상속)
    ├── Bio.java                 # 바이오 제품 공통 클래스 (Product 상속)
    ├── Smartphone.java          # 스마트폰 (Electronics 상속)
    ├── Perfume.java             # 향수 (Bio 상속)
    └── Bed.java                 # 침대 (Furniture 상속)
```

## 시나리오

### 1단계: 제품 등록
- 스마트폰, 향수, 침대 중 선택
- 제품별 상세 정보 입력 (브랜드, 향, 용량 등)
- 생산할 수량 설정

### 2단계: 생산 라인 설정
- 1-5개 생산 라인 선택
- 동시 실행할 스레드 수 결정

### 3단계: 병렬 생산 실행
- 각 제품을 별도 스레드에서 동시 생산
- 제품별 고유 생산 과정 수행
- 실시간 진행 상황 모니터링

### 4단계: 결과 확인
- 생산 완료 통계 확인
- 완료된 제품 목록 조회

## 설치 및 실행

### 요구사항
- Java 11 이상
- Gradle (또는 Gradle Wrapper 사용)

### 빌드 및 실행

```bash
# 프로젝트 클론
git clone <repository-url>
cd java-cli-project

# Gradle Wrapper를 사용한 빌드
./gradlew build

# 애플리케이션 실행
./gradlew run
```

또는 직접 Java 실행:

```bash
# 컴파일
./gradlew compileJava

# 실행
java -cp build/classes/java/main production.simulator.Main
```