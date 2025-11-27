# 🤖 RULES.md – Collaborative Study Platform (Jaroslav Birčák)

## 🎯 Účel
Tento dokument definuje **pracovné pravidlá pre Cursor AI** počas vývoja projektu.  
Cieľom je, aby Cursor:
- **nezasahoval do funkčného kódu,**
- **zapisoval každú zmenu do CHANNEL_LOG.md,**
- **pomáhal rozširovať, opravovať a dokumentovať** projekt bezpečne.

---

## 🧭 1. Základné princípy pre Cursor

1. 🔒 **NEUPRAVUJ existujúci funkčný kód.**  
   Cursor nesmie meniť:
   - sekcie označené `// working` alebo `// stable`,  
   - triedy, ktoré už úspešne kompilujú a fungujú,  
   - logiku autentifikácie, databázové entity, konfiguračné súbory.

2. 🧩 **Generuj len to, čo chýba.**  
   Cursor dopĺňa iba:
   - metódy označené `// TODO`,  
   - prázdne bloky kódu,  
   - nové komponenty (napr. controller, service, view),  
   - dokumentáciu a testy.

3. 🧠 **Zachovaj štruktúru projektu.**  
   - Nemen názvy priečinkov alebo tried.  
   - Nové súbory vytváraj iba v `/client`, `/server`, `/database`, `/docs`.  
   - Používaj rovnaké konvencie názvov ako zvyšok projektu.

4. 💬 **Všetko nové musí byť komentované.**  
   Každý blok generovaný AI musí obsahovať komentár:  
   ```java
   // Added by Cursor AI – explains what the block does