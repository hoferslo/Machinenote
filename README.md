# MachineNote - Android Application

## Pregled aplikacije

MachineNote je Android aplikacija za upravljanje strojev in vzdrževalnih del. Aplikacija omogoča uporabnikom upravljanje z naloge, pregledovanje opreme, spremljanje zastojev, upravljanje rezervnih delov in izvajanje preventivnih pregledov.

## Arhitektura aplikacije

Aplikacija sledi standardnemu Android MVP (Model-View-Presenter) vzorcu in uporablja:
- **MainActivity**: Glavna aktivnost z navigation drawer-jem za navigacijo med fragmenti
- **Fragmenti**: Različni fragmenti za različne funkcionalnosti (Dashboard, Login, QR skener)
- **ApiManager**: Osrednji manager za komunikacijo z REST API
- **ApiClient**: HTTP klient za mrežne zahteve (Retrofit)
- **SharedPreferencesHelper**: Lokalno shranjevanje uporabniških nastavitev

---

## ApiManager - Upravljanje API komunikacije

### Inicializacija

```java
// Standardna inicializacija
ApiManager apiManager = new ApiManager(context);

// Inicializacija s prilagojenim timeout-om
ApiManager apiManager = new ApiManager(context, milliseconds);
```

### Avtentikacija

#### Prijava uporabnika
```java
apiManager.login(username, password, new LoginCallback() {
    @Override
    public void onSuccess() {
        // Uspešna prijava - API ključ je samodejno shranjen
        // Uporabnik je preusmeren na dashboard
    }
    
    @Override
    public void onFailure(String errorMessage) {
        // Napaka pri prijavi
        Log.e("Login", errorMessage);
    }
});
```

**Kaj se zgodi pri prijavi:**
1. Pošlje se `LoginRequest` z username/password
2. Ob uspešnem odgovoru se shrani API ključ v SharedPreferences
3. API ključ se nastavi v ApiClient za nadaljnje zahteve
4. Shranijo se podatki o uporabniku (username, password, role)

---

## Upravljanje podatkov

### Preventivni pregledi

#### Pridobitev vseh pregledov
```java
apiManager.getPreventivniPregledi(new PreventivniPreglediCallback() {
    @Override
    public void onSuccess(List<PreventivniPregled> pregledi) {
        // Seznam vseh preventivnih pregledov
    }
    
    @Override
    public void onFailure(String errorMessage) {
        // Napaka pri pridobitvi
    }
});
```

#### Pridobitev opravil za določen pregled
```java
apiManager.getOpravilaForPregled(pregledId, new OpravilaCallback() {
    @Override
    public void onSuccess(List<PregledOpravilo> opravila) {
        // Seznam opravil za pregled
    }
    
    @Override
    public void onFailure(String errorMessage) {
        // Napaka pri pridobitvi opravil
    }
});
```

### Zastoji

#### Pridobitev zastojev
```java
apiManager.getZastoji(new ZastojiCallback() {
    @Override
    public void onSuccess(List<Zastoj> zastoji) {
        // Seznam zastojev
    }
    
    @Override
    public void onFailure(String errorMessage) {
        // Napaka pri pridobitvi
    }
});
```

#### Pošiljanje zastoja
```java
// Osnovni zastoj brez slik
apiManager.sendZastoj(zastoj, new Callback<Void>() {
    @Override
    public void onResponse(Call<Void> call, Response<Void> response) {
        // Uspešno poslano
    }
    
    @Override
    public void onFailure(Call<Void> call, Throwable t) {
        // Napaka pri pošiljanju
    }
});

// Zastoj s slikami
List<File> imageFiles = Arrays.asList(imageFile1, imageFile2);
apiManager.sendZastojWithImages(zastoj, imageFiles, callback);
```

### Rezervni deli

#### Pridobitev rezervnih delov
```java
apiManager.getRezervniDeli(new RezervniDeliCallback() {
    @Override
    public void onSuccess(List<RezervniDel> rezervniDeli) {
        // Seznam rezervnih delov
    }
    
    @Override
    public void onFailure(String errorMessage) {
        // Napaka
    }
});
```

#### Upravljanje zaloge
```java
// Prilagodi zalogo (+ ali - količina)
apiManager.adjustStock(delId, spremembaKolicine, new StockAdjustmentCallback() {
    @Override
    public void onResponse(Call<Void> call, Response<Void> response) {
        // Zaloga uspešno prilagojena
    }
    
    @Override
    public void onFailure(Call<Void> call, Throwable throwable) {
        // Napaka pri prilagajanju zaloge
    }
});
```

### Naloge

#### Pridobitev nalog
```java
apiManager.getNaloge(new NalogaCallback() {
    @Override
    public void onSuccess(List<Naloga> naloge) {
        // Seznam nalog
    }
    
    @Override
    public void onFailure(String message) {
        // Napaka
    }
});
```

#### Pošiljanje nove naloge s slikami
```java
List<File> images = Arrays.asList(slika1, slika2);
apiManager.sendNalogaWithImages(naloga, images, new Callback<Void>() {
    @Override
    public void onResponse(Call<Void> call, Response<Void> response) {
        // Naloga uspešno poslana
    }
    
    @Override
    public void onFailure(Call<Void> call, Throwable t) {
        // Napaka pri pošiljanju
    }
});
```

### Naročila

#### Upravljanje naročil
```java
// Pridobi naročila
apiManager.getNarocila(new NarocilaCallback() {
    @Override
    public void onSuccess(List<Narocila> narocila) {
        // Seznam naročil
    }
    
    @Override
    public void onFailure(String message) {
        // Napaka
    }
});

// Pošlji novo naročilo s slikami
apiManager.sendNarocilaWithImages(narocila, imageFiles, callback);

// Posodobi naročilo
apiManager.updateNarocilaWithImages(id, narocila, imageFiles, callback);
```

---

## ApiClient - HTTP klient

ApiClient uporablja Retrofit za HTTP komunikacijo in zagotavlja:

- **Avtomatsko dodajanje API ključa**: Ko se uporabnik prijavi, se API ključ doda v vse zahteve
- **Timeout konfiguracija**: Nastavljiv timeout za mrežne zahteve
- **JSON serializacija**: Samodejno pretvarjanje med Java objekti in JSON
- **Multipart upload**: Podpora za pošiljanje datotek (slike)

### Osnovne funkcionalnosti:
```java
// Nastavi API ključ (se zgodi samodejno pri prijavi)
ApiClient.setApiKey("your-api-key");

// Dobi Retrofit klienta
Retrofit client = ApiClient.getClient();

// Klient s prilagojenim timeout-om
Retrofit client = ApiClient.getClient(timeoutMilliseconds);
```

---

## Omrežna povezava

### Preverjanje povezave s strežnikom
```java
apiManager.checkServerConnection(new ConnectionCallback() {
    @Override
    public void onSuccess() {
        // Povezava s strežnikom je vzpostavljena
    }
    
    @Override
    public void onFailure(String errorMessage) {
        // Ni povezave s strežnikom
    }
});
```

MainActivity samodejno preverja povezavo s strežnikom in prikazuje indikator, če ni povezave.

---

## Upravljanje uporabnikov in vlog

### Registracija novega uporabnika
```java
RegistrationRequest request = new RegistrationRequest(username, password, role);
apiManager.registerUser(request, new RegistrationCallback() {
    @Override
    public void onSuccess() {
        // Uporabnik uspešno registriran
    }
    
    @Override
    public void onFailure(String errorMessage) {
        // Napaka pri registraciji
    }
});
```

### Upravljanje vlog
```java
// Pridobi vse vloge
apiManager.getRoles(new Callback<List<Role>>() {
    // Implementacija callback-a
});

// Ustvari novo vlogo
List<String> permissions = Arrays.asList("read", "write", "delete");
apiManager.createRole("NazivVloge", permissions, new RoleCreationCallback() {
    // Implementacija callback-a
});
```

---

## Posebnosti implementacije

### Varnost
- Vsi API klici zahtevajo avtentikacijo z API ključem
- API ključ se shrani lokalno v SharedPreferences
- Ob odjavi se vsi lokalni podatki počistijo

### Delo s slikami
- Podpora za multipart upload slik
- Slike se pretvorijo v `MultipartBody.Part` format
- Uporabljajo se različni field names: `images[]`, `completion_images[]`

### Error handling
- Vsi API klici imajo callback strukture z `onSuccess` in `onFailure`
- Podrobno beleženje napak v logove
- Prikazovanje uporabniku prijaznih sporočil o napakah

### Povezava s strežnikom
- Redno preverjanje povezave (vsake 1.5 sekunde)
- Prikaz indikatorja za stanje povezave
- Samodejno ponovno vzpostavljanje funkcionalnosti ob obnovitvi povezave

### Lokalno shranjevanje
- SharedPreferences za uporabniške nastavitve
- Shranjevanje API ključa, uporabniškega imena, gesla in vloge
- Čiščenje podatkov ob odjavi

---

## Callback strukture

Aplikacija uporablja standardne callback vzorce:

```java
public interface LoginCallback {
    void onSuccess();
    void onFailure(String errorMessage);
}

public interface DataCallback<T> {
    void onSuccess(T data);
    void onFailure(String errorMessage);
}
```

To omogoča asinhrono komunikacijo z API-jem in ustrezno upravljanje uporabniškega vmesnika.
