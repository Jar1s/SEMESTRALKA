# 🧪 Testovanie - CALLSTUDY

Tento dokument popisuje automatizované testy a CI/CD pipeline pre projekt CALLSTUDY.

## 📋 Prehľad

Projekt obsahuje:
- **Unit testy** pre server services a client utility triedy
- **Integračné testy** pre REST API endpoints
- **CI/CD pipeline** s automatickým testovaním a buildovaním

## 🏗️ Štruktúra testov

```
server/src/test/java/
├── sk/ikts/server/
│   ├── service/
│   │   ├── UserServiceTest.java
│   │   └── GroupServiceTest.java
│   └── controller/
│       └── UserControllerIntegrationTest.java
└── resources/
    └── application-test.properties

client/src/test/java/
└── sk/ikts/client/util/
    └── ApiClientTest.java
```

## 🚀 Spustenie testov

### Spustenie všetkých testov

```bash
# Server testy
cd server
mvn test

# Client testy
cd client
mvn test

# Všetky testy z root adresára
mvn test
```

### Spustenie konkrétneho testu

```bash
# Server
cd server
mvn test -Dtest=UserServiceTest

# Client
cd client
mvn test -Dtest=ApiClientTest
```

### Spustenie s coverage reportom

```bash
# Server s JaCoCo coverage
cd server
mvn clean test jacoco:report

# Výsledok je v: server/target/site/jacoco/index.html
```

## 📊 Test Coverage

Projekt používa **JaCoCo** pre meranie pokrytia kódu testami.

### Zobrazenie coverage reportu

1. Spustite testy s coverage:
   ```bash
   cd server
   mvn clean test jacoco:report
   ```

2. Otvorte HTML report:
   ```bash
   open server/target/site/jacoco/index.html
   ```

### Cieľové pokrytie

- **Minimálne pokrytie:** 50% riadkov kódu
- **Odporúčané pokrytie:** 70%+ pre kritické komponenty

## 🔄 CI/CD Pipeline

### GitHub Actions Workflow

CI/CD pipeline sa automaticky spúšťa pri:
- Push do `main` alebo `develop` branch
- Pull request do `main` alebo `develop` branch
- Manuálne spustenie cez GitHub Actions

### Workflow kroky

1. **Test Job**
   - Spustí unit testy pre server a client
   - Generuje coverage reporty
   - Uploaduje test výsledky

2. **Build Job**
   - Kompiluje server a client
   - Vytvára JAR súbory
   - Uploaduje artifacts

3. **Integration Test Job**
   - Spustí server
   - Spustí integračné testy
   - Overí funkčnosť API

4. **Code Quality Job**
   - Kontroluje kvalitu kódu
   - Overuje formátovanie

### Zobrazenie výsledkov

1. Prejdite na GitHub → Actions tab
2. Vyberte workflow run
3. Kliknite na konkrétny job pre detaily

## 📝 Typy testov

### Unit Testy

Testujú jednotlivé komponenty izolovane pomocou mockov.

**Príklady:**
- `UserServiceTest` - testuje business logiku registrácie a prihlásenia
- `GroupServiceTest` - testuje správu skupín
- `ApiClientTest` - testuje HTTP komunikáciu

### Integračné Testy

Testujú interakciu medzi komponentami a REST API.

**Príklady:**
- `UserControllerIntegrationTest` - testuje REST endpoints pre používateľov

### Testovacie závislosti

- **JUnit 5** - testovací framework
- **Mockito** - mockovanie objektov
- **MockWebServer** - mock HTTP server pre client testy
- **Spring Boot Test** - testovanie Spring aplikácií
- **JaCoCo** - code coverage

## 🛠️ Pridanie nových testov

### Vytvorenie unit testu pre service

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    
    @Mock
    private MyRepository repository;
    
    @InjectMocks
    private MyService service;
    
    @Test
    void testMyMethod() {
        // Arrange
        when(repository.findById(anyLong())).thenReturn(Optional.of(mockObject));
        
        // Act
        Result result = service.myMethod(1L);
        
        // Assert
        assertNotNull(result);
        verify(repository).findById(1L);
    }
}
```

### Vytvorenie integračného testu

```java
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class MyControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testEndpoint() throws Exception {
        mockMvc.perform(get("/api/endpoint"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.field").value("expected"));
    }
}
```

## 📈 Best Practices

1. **Názvy testov** - používajte deskriptívne názvy: `testMethodName_Scenario_ExpectedResult`
2. **AAA Pattern** - Arrange, Act, Assert
3. **Izolácia** - každý test by mal byť nezávislý
4. **Mocking** - mockujte externé závislosti
5. **Coverage** - ciele na pokrytie kritických ciest
6. **Rýchlosť** - unit testy by mali byť rýchle (< 1s)

## 🐛 Troubleshooting

### Testy neprechádzajú v CI

1. Skontrolujte lokálne spustenie:
   ```bash
   mvn clean test
   ```

2. Skontrolujte logy v GitHub Actions

3. Overte testovaciu konfiguráciu v `application-test.properties`

### Coverage report sa negeneruje

1. Skontrolujte, či je JaCoCo plugin správne nakonfigurovaný v `pom.xml`
2. Spustite `mvn clean` pred testovaním
3. Overte, či sú testy skutočne spustené

### Integračné testy zlyhávajú

1. Skontrolujte, či server beží správne
2. Overte databázové pripojenie v test profile
3. Skontrolujte porty a konfiguráciu

## 📚 Ďalšie zdroje

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)

---

**Poznámka:** Toto je voliteľný prídavok pre pokročilých študentov. Základná funkcionalita projektu funguje aj bez testov.

