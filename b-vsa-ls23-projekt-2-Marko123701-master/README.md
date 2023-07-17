[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/UmRRiQ2w)
# B-VSA LS 22/23 - Semestrálny projekt 2

![Java 1.8](https://img.shields.io/badge/Java-1.8-red)
![EclipseLink 2.7.11](https://img.shields.io/badge/EclipseLink-2.7.11-blue)
![PostgreSQL Driver](https://img.shields.io/badge/PostgreSQL-42.5.4-green)
![Jersey JAX-RS](https://img.shields.io/badge/Jersey-2.39.1-orange)
[![Public domain](https://img.shields.io/badge/License-Unlicense-lightgray)](https://unlicense.org)

Cieľom 2. semestrálneho projektu je implementovať jednoduchú webovú aplikáciu publikujúcu REST API podľa definovanej
špecifikácie. Projekt má nadväzovať na 1. semestrálny projekt, v ktorom ste implementovali spojenie s SQL databázou
použitím technológie JPA, takže aj v tomto projekte majú byť dodržané vzťahy a logika medzi jednotlivými entitami.

V tomto projekte môžte vychádzať z vášho riešenia 1. semestrálneho projektu, alebo využiť poskytnutú [referenčnú implementáciu](https://github.com/Interes-Group/b-vsa-ls23-project1-solution).
Zachovajte asociácie a objekty definované v 1. projekte. Nakoľko účelom tohto zadania je implementácia REST webových služieb,
môžte upraviť riešenie z predchádzajúceho zadania podľa svojho najlepšieho vedomia a svedomia, tak aby splnilo toto zadanie.
Pre vypracovanie sa môžte inšpirovať [projektami z cvičení](https://github.com/Interes-Group/b-vsa-cvicenia). 
Dbajte na dodržiavanie pokynov zadania nakoľko bude opravované automatizovane pomocou JUnit testov. Na konci zadania je návod, 
ako si lokálne spustiť tzv. "sanity-check" test, pre otestovanie splnenia konfigurácie vypracovania, aby bol projekt testovateľný.

## Špecifikácia

Aplikácia musí publikovať **REST webové služby** dodržiavajúce dodanú [**OpenAPI 3 špecifikáciu**](./src/main/resources/openapi3.spec.yaml). 
Aplikácia tak musí zabezpečiť CRUD operácie cez publikované webové služby (s výnimkou update operácie). Aplikácia musí
taktiež poskytovať webové služby pre priradenie a odovzdanie záverečnej práce, a tak isto pre vyhľadanie záverečnej práce
podľa platných kritérií (t.j. podľa študenta, alebo podľa učiteľa).

Pri implementácií použite protokol **HTTP/1.1**, ako formát prenášaných objektov použite **application/json** s UTF-8 kódovaním. 
Pri chybových stavoch vráťte objekt `Message` (ako je uvedené v špecifikácii) s príslušným kódom chyby. 
Kód odpovede nastavte tak, aby najlepšie vystihla povahu odpovede. 
HTTP kódy: [https://www.restapitutorial.com/httpstatuscodes.html](https://www.restapitutorial.com/httpstatuscodes.html).

### Autentifikácia

V rámci projektu implementujte **autentifikačný mechanizmus Basic Access Authentication** [https://en.wikipedia.org/wiki/Basic_access_authentication](https://en.wikipedia.org/wiki/Basic_access_authentication)
(skrátene Basic Auth). Jednotlivé webové služby sú v špecifikácii označené či je potrebná autentifikácia pre ich dopytovanie. 
Používateľom riešenia, ktorým je umožnené prihlásenie, je každý vytvorený študent a učiteľ.
Ako prihlasovacie meno používateľa použite email učiteľa alebo študenta. Entitu študenta a učiteľa rozšírte o atribút `password`. 
Pri vytvorení používateľa musí byť zadané heslo. Heslo v dopyte vytvorenia používateľa nesmie byť poslané v otvorenej podobe 
ale ako string enkódovaný v BASE64 kódovaní.

Pred uložením do databázy musí byť heslo dekódované do pôvodnej hodnoty vypočítaný hash a následne až výsledný hash reťazec
uložený do databázy ako hodnota `password` atribútu. Pri prihlásení používateľa je porovnané heslo z HTTP hlavičky `Authorization`
s uloženým hash reťazcom z databázy. Autentifikačný mechanizmus musí dokázať rozlíšiť či ide o študenta alebo učiteľa.
Pre vytvorenie hash reťazca hesla a následnú verifikáciu pri prihlásení môžte využiť metódy triedy [BCryptServer](./src/main/java/sk/stuba/fei/uim/vsa/pr2/BCryptService.java).

REST služby implementujte ako bezstavové. Nevytvárajte žiadnu používateľskú reláciu (Session), nevytvárajte žiadne autorizačné tokeny, 
či iné spôsoby relácie a udržiavania stavu používateľa (ako napr. cookies). HTTP hlavička `Authorization` musí byť zaslaná s každým dopytom na služby.

Zlyhanie prihlásenia má vrátiť odpoveď s kódom 401.

### Autorizácia

V rámci projektu implementujte **autorizačný mechanizmus**, ktorý bude **rozlišovať medzi prístupom učiteľa a študenta**.
V [dodanej OpenAPI 3 špecifikácii](./src/main/resources/openapi3.spec.yaml) pri niektorých REST webových službách je popísané,
či má byť implementované obmedzenie pre prístup prihláseného používateľa (napr. pri službe `DELETE /api/student/{id}`). 
Ak nie je zmienené žiadne takéto obmedzenie pri službe, môže ju dopytovať učiteľ aj študent a spracovanie ich dopytu musí
byť rovnaké. Rozlíšenie prístupu určte na základe autentifikovanej entity. 

V prípade nedostatočných oprávnení prihláseného používateľa pre vykonanie dopytu vráťte odpoveď s kódom 403.

## Spustenie

Webovú aplikáciu implementujte pomocou frameworku Jersey (referenčná implementácie JAX-RS štandardu) ako tzv. **'standalone'
webovú aplikáciu**. Výstupom riešenia musí byť spustiteľný JAR súbor. HTTP server aplikácie má počúvať na porte 8080.

Konfiguráciu spustenia HTTP servera je možné nájsť v triede [ApplicationConfiguration](./src/main/java/sk/stuba/fei/uim/vsa/pr2/ApplicationConfiguration.java). 
Jej hodnoty je možné meniť pomocou tzv. 'environment variables'. Východzie hodnoty jednotlivých atribútov nemeňte.

Triedy obsahujúce definície jednotlivých služieb (tzv. 'resource class') anotované anotáciou `@Path` a triedy rozširujúce
funkcionalitu HTTP servera anotované anotáciou `@Provider` sú automaticky skenované a pridané do HTTP servera pri štarte
triedou [JAXRSApplicationConfiguration](./src/main/java/sk/stuba/fei/uim/vsa/pr2/JAXRSApplicationConfiguration.java).
Implementáciu tejto triedy nie je nutné meniť.

Embedovaný HTTP server je nakonfigurovaný a zapnutý v hlavnej triede [Project2Application](./src/main/java/sk/stuba/fei/uim/vsa/pr2/Project2Application.java).
Ak je potrebná inicializácia, alebo je potrebné spustiť ľubovolný kód pri štarte aplikácie (tesne po štarte HTTP servera)
je možné využiť metódu `postStart()` v tejto triede. Implementáciu metód `main` a `startServer` nemeňte.

## Hodnotenie

**Zadanie je hodnotené 15 bodmi. Vypracovanie je nutné odovzdať do 10.05.2023 23:59.**

Zadanie si naklonujte z repozitára zadania. Svoje vypracovanie nahrajte do vášho repozitára pre toto zadanie pomocou
programu Git (git commit + git push). Vypracovanie môžete "pusho-vať" priebežne. Názov Java balíčka nemeňte. 
Nepresúvajte ani nemeňte súbory pripravené v zadaní pokiaľ nie je stanovené inak alebo si to implementácia riešenia
zadania vyslovene vyžaduje (napr. doplnenie tried entít (`<class>` tag) v súbore `persistence.xml`).

**Úpravy pom.xml súbory sú zakázané** mimo ďalej uvedených zmien:

- Pridanie závislosti na driver databázy podľa vlastného výberu. Povolené SQL databázy:
    - MySQL
    - OracleDB
    - Derby
    - PostgreSQL (bude použitá ako testovacia databáza pri oprave)
    - H2
- Doplnenie informácií o autorovi (developerovi, tag `<developers><developer>`)

**Názov `persistence-unit`** v súbore [persistence.xml](src/main/resources/META-INF/persistence.xml) **nemeňte**. Pre
vlastné otestovanie aplikácie môžte implementovať vlastné jUnit testy v priečinku `src/test/java` alebo využiť IntelliJ
HTTP klienta v priečinku `src/test/http`.
Použitá databáza sa musí volať **vsa_pr2** a musí mať vytvoreného používateľa s **menom 'vsa' a heslom 'vsa'**. Pre
tieto účeli môžte využiť súbor [starter.postgres.sql](configs/starter.postgres.sql) (určený pre databázu PostgreSQL).

Implementované webové služby musia konzumovať telá dopytov a produkovať odpovede vo formáte JSON (application/json). 
V rámci projektu je spracovanie JSON formátu už nakonfigurované.

Hodnotiť sa bude iba master/main branch. Kvôli testom a zrýchleniu opravovania je nutné dodržať pokyny a štruktúru
projektu, ako je stanovené v zadaní! Iba kód poslednej verzie vypracovania (t.j. z posledného commit-u) do termínu
odovzdania sa berie do úvahy. Okrem testov sa bude kód a funkcionalita kontrolovať aj manuálne. Hodnotiť sa budú iba
skompilovateľné a spustiteľné riešenia!

### Sanity check test

Projekt obsahuje test tzv. "sanity-check", ktorý kontroluje, či váš projekt dodržuje pokyny zadania a tak či bude 
akceptovaný na opravu. Test je napísaný v triede [SanityCheckTest](src/test/java/sk/stuba/fei/uim/vsa/pr2/SanityCheckTest.java).
Pre spustenie testu stačí vykonať **maven** lifecycle goal **test**, resp. spustiť príkaz:

```shell
mvn test
```

Alebo otvoriť testovací súbor a spustiť jUnit test (zelená šípka vedľa názvu triedy). Ak test skončí úspešne (t.j. bez chyby),
váš projekt je pripravený na opravu.

### Logovanie a výpisy

V rámci projektu je nakonfigurovaná knižnica pre logovanie (formátované výpisy) [Logback](https://www.baeldung.com/logback). Túto knižnicu môžte použiť namiesto
Java `System.out.println` pre formátovaný výpis (napríklad ako je v `SanityCheckTest` triede). Táto knižnica je využitá
ako primárny spôsob výpisu pre pripravené triedy projektu.

Knižnicu je potrebné inicializovať v každej triede kde ju chcete použiť riadkom:

```Java
private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(<názov triedy ktorú treba logovať>.class);
```

Následne je možné využiť metódy ako `log.info`, `log.warn` či `log.error` v prípade výnimiek.

## Bonus (3b)

V rámci zadania máte možnosť implementovať rozšírenie funkcionality o tzv. stránkovanie odpovedí služieb za 3 bonusové body.
Pre implementáciu bonusu implementujte [OpenAPI 3 špecifikáciu bonusových webových služieb](./src/main/resources/bonus-openapi3.spec.yaml)
s rozšírením stránkovania. Stránkovanie má pracovať s vašou implementáciou rozhraní Page a Pageable z 1. semestrálneho projektu.
Bonusová špecifikácia zahŕňa novú definíciu webovej služby `GET /api/search/theses`, ktorá nahrádza predchádzajúcu definíciu tejto webovej
služby zo zadania.

Bonusové vypracovanie bude uznané jedine v prípade ak je implementované celé zadanie.
